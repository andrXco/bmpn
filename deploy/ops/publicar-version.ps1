[CmdletBinding(DefaultParameterSetName = 'Release')]
param(
    [Parameter(Mandatory, ParameterSetName = 'Release')]
    [ValidatePattern('^v(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)$')]
    [string] $Version,

    [Parameter(Mandatory, ParameterSetName = 'Development')]
    [switch] $Development,

    [Parameter(Mandatory)]
    [ValidatePattern('^[A-Za-z0-9._-]+@[A-Za-z0-9._-]+$')]
    [string] $SshTarget,

    [switch] $VerifyOnly
)

$ErrorActionPreference = 'Stop'
$targetEnvironment = if ($Development) { 'development' } else { 'staging' }
$branch = if ($Development) { 'development' } else { 'deploy' }
$port = if ($Development) { 18081 } else { 18080 }
$imageNamespace = if ($Development) { 'bmpn-development' } else { 'bmpn-release' }

function Invoke-GitHubApi {
    param([string[]] $Arguments, [hashtable] $Body)

    if ($null -eq $Body) {
        $output = gh api @Arguments
    } else {
        $output = $Body | ConvertTo-Json -Depth 6 -Compress | gh api @Arguments --input -
    }
    if ($LASTEXITCODE -ne 0) { throw "Falló la consulta GitHub: $($Arguments -join ' ')" }
    if ($output) { ConvertFrom-Json -InputObject ($output -join [Environment]::NewLine) }
}

function Get-ReleaseCommit {
    param([string] $Repository, [string] $Tag)

    $lines = git ls-remote --tags "https://github.com/$Repository.git" "refs/tags/$Tag" "refs/tags/$Tag^{}"
    if ($LASTEXITCODE -ne 0) { throw "No se pudo consultar el tag $Tag de $Repository." }
    if ($lines) { (($lines | Select-Object -Last 1) -split '\s+')[0] }
}

$healthProbe = @'
set -euo pipefail
project="bmpn-$1"
port=$2
image_namespace=$3
cd "$HOME/$project"
for component in app front; do
  case "$component" in
    app) key=BACKEND_IMAGE; image_prefix="$image_namespace-backend" ;;
    front) key=FRONT_IMAGE; image_prefix="$image_namespace-front" ;;
  esac
  container="$project-$component-1"
  image=$(docker inspect --format '{{.Config.Image}}' "$container")
  [[ "$image" =~ ^$image_prefix:[a-f0-9]{40}$ ]]
  [[ "$(docker inspect --format '{{.State.Health.Status}}' "$container")" == healthy ]]
  grep -Fxq "$key=$image" images.env
  printf '%s=%s\n' "$key" "${image#*:}"
done
[[ "$(docker inspect --format '{{.State.Health.Status}}' "$project-postgres-1")" == healthy ]]
curl -fsS --max-time 10 "http://127.0.0.1:$port/healthz" >/dev/null
status=$(curl -sS --max-time 10 -o /dev/null -w '%{http_code}' "http://127.0.0.1:$port/api/empresas")
[[ "$status" == 401 || "$status" == 403 ]]
'@

$versions = $healthProbe | ssh -o BatchMode=yes -o StrictHostKeyChecking=yes -o ConnectTimeout=8 $SshTarget "tr -d '\r' | bash -s -- $targetEnvironment $port $imageNamespace"
if ($LASTEXITCODE -ne 0) { throw "$targetEnvironment no pasó la comprobación por SSH." }
$components = @(
    @{ Repository = 'andrXco/bmpn'; ImageKey = 'BACKEND_IMAGE' },
    @{ Repository = 'andrXco/bmpn-react'; ImageKey = 'FRONT_IMAGE' }
)

foreach ($component in $components) {
    $repository = $component.Repository
    $prefix = "$($component.ImageKey)="
    $line = $versions | Where-Object { $_.StartsWith($prefix) } | Select-Object -First 1
    if ($line -notmatch '^[A-Z_]+=([a-f0-9]{40})$') { throw "No se pudo validar el SHA de $repository." }
    $sha = $Matches[1]
    $component.Sha = $sha
    $ref = Invoke-GitHubApi -Arguments @("repos/$repository/git/ref/heads/$branch")
    $head = $ref.object.sha
    if ($head -ne $sha) { throw "$targetEnvironment todavía no está en la punta de $branch de $repository." }
    $runs = gh run list -R $repository -b $branch -w CI --commit $sha --event push --status success --limit 1 --json url
    if ($LASTEXITCODE -ne 0) { throw "No se pudo consultar el CI de $repository." }
    $successfulRun = @($runs | ConvertFrom-Json) | Select-Object -First 1
    $component.CiUrl = $successfulRun.url
    if ($Development) { continue }
    $releases = Invoke-GitHubApi -Arguments @("repos/$repository/releases/tags/staging-$sha")
    $assetNames = @($releases.assets | ForEach-Object { $_.name })
    if (-not $releases.prerelease -or $assetNames -notcontains 'image.tar.gz' -or $assetNames -notcontains 'image.tar.gz.sha256') {
        throw "Faltan los archivos de staging de $repository para $sha."
    }
    if (-not $successfulRun.url) { throw "No se encontró un CI exitoso para $repository en $sha." }
    $existingCommit = Get-ReleaseCommit -Repository $repository -Tag $Version
    if ($existingCommit -and $existingCommit -ne $sha) {
        throw "$Version ya existe en $repository y apunta a otro commit."
    }
}

if ($VerifyOnly) {
    foreach ($component in $components) {
        Write-Output "$($component.Repository) $($component.Sha) verificado"
    }
    return
}

foreach ($component in $components) {
    $repository = $component.Repository
    $sha = $component.Sha
    $settings = Invoke-GitHubApi -Arguments @("repos/$repository/environments")
    if ($Development) {
        $configured = $settings.environments | Where-Object { $_.name -eq 'development' }
        if (-not $configured.deployment_branch_policy.custom_branch_policies) {
            $null = Invoke-GitHubApi -Arguments @('--method', 'PUT', "repos/$repository/environments/development") -Body @{
                deployment_branch_policy = @{ protected_branches = $false; custom_branch_policies = $true }
            }
        }
        $policyPath = "repos/$repository/environments/development/deployment-branch-policies"
        $policies = Invoke-GitHubApi -Arguments @($policyPath)
        if (-not ($policies.branch_policies | Where-Object { $_.name -eq 'development' -and $_.type -eq 'branch' })) {
            $null = Invoke-GitHubApi -Arguments @('--method', 'POST', $policyPath) -Body @{ name = 'development'; type = 'branch' }
        }
    } else {
        $names = @($settings.environments | ForEach-Object { $_.name })
        foreach ($environment in @('staging', 'production')) {
            if ($names -notcontains $environment) {
                $null = Invoke-GitHubApi -Arguments @('--method', 'PUT', "repos/$repository/environments/$environment") -Body @{}
            }
        }
    }

    $deployments = @(Invoke-GitHubApi -Arguments @("repos/$repository/deployments?environment=$targetEnvironment&sha=$sha&per_page=20"))
    $deployment = $deployments | Where-Object { $_.sha -eq $sha } | Select-Object -First 1
    if (-not $deployment) {
        $deploymentRef = if ($Development) { 'development' } else { $sha }
        $deployment = Invoke-GitHubApi -Arguments @('--method', 'POST', "repos/$repository/deployments") -Body @{
            ref = $deploymentRef
            environment = $targetEnvironment
            auto_merge = $false
            required_contexts = @()
            production_environment = $false
            transient_environment = $false
            description = "BPMN $targetEnvironment comprobado en Ubuntu"
            payload = @{ branch = $branch; release = $Version }
        }
    }
    if (-not $deployment.id) { throw "GitHub no devolvió el ID del deployment de $repository." }
    if ($deployment.sha -ne $sha) { throw "La rama cambió antes de registrar el deployment de $repository." }
    $statuses = @(Invoke-GitHubApi -Arguments @("repos/$repository/deployments/$($deployment.id)/statuses?per_page=1"))
    if (($statuses | Select-Object -First 1).state -ne 'success') {
        $statusBody = @{
            state = 'success'
            description = "$targetEnvironment verificado en Ubuntu con Angular, Spring Boot y PostgreSQL sanos"
            auto_inactive = $true
        }
        if ($component.CiUrl) { $statusBody.log_url = $component.CiUrl }
        $status = Invoke-GitHubApi -Arguments @('--method', 'POST', "repos/$repository/deployments/$($deployment.id)/statuses") -Body $statusBody
        if ($status.state -ne 'success') { throw "GitHub no confirmó $targetEnvironment en $repository." }
    }

    if ($Development) {
        Write-Output "$repository $sha development=success"
        continue
    }

    gh release view $Version -R $repository --json tagName 2>$null | Out-Null
    if ($LASTEXITCODE -ne 0) {
        $peer = $components | Where-Object { $_.Repository -ne $repository } | Select-Object -First 1
        $assetRoot = "https://github.com/$repository/releases/download/staging-$sha"
        $notesPath = New-TemporaryFile
        try {
            @(
                "Versión BPMN $Version verificada en staging."
                ''
                ('- Commit: `{0}`' -f $sha)
                "- [Release del otro componente](https://github.com/$($peer.Repository)/releases/tag/$Version)"
                "- [Imagen Docker]($assetRoot/image.tar.gz) y [SHA256]($assetRoot/image.tar.gz.sha256)"
                "- [Ejecución de CI]($($component.CiUrl))"
                ''
                'La promoción a producción se realiza por separado.'
            ) | Set-Content -LiteralPath $notesPath.FullName -Encoding utf8
            gh release create $Version -R $repository --target $sha --latest --title "BPMN $Version" --notes-file $notesPath.FullName
            if ($LASTEXITCODE -ne 0) { throw "No se pudo crear el release de $repository." }
        } finally {
            Remove-Item -LiteralPath $notesPath.FullName -ErrorAction SilentlyContinue
        }
    }
    if ((Get-ReleaseCommit -Repository $repository -Tag $Version) -ne $sha) {
        throw "El tag de $Version no coincide con el SHA verificado de $repository."
    }
    Write-Output "$repository $Version $sha staging=success"
}
