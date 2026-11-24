$ErrorActionPreference = 'Stop'

$databaseUser = if ($env:POSTGRES_USER) { $env:POSTGRES_USER } else { 'bmpn' }
$databaseName = if ($env:POSTGRES_DB) { $env:POSTGRES_DB } else { 'bmpn' }

$serviceId = docker compose ps -q postgres
if (-not $serviceId) {
    throw 'PostgreSQL no esta iniciado. Ejecuta: docker compose up -d --wait'
}

$health = docker inspect --format '{{.State.Health.Status}}' $serviceId
if ($health -ne 'healthy') {
    throw "PostgreSQL no esta saludable (estado: $health)."
}

Get-Content -Raw -LiteralPath 'scripts/database/database-smoke-test.sql' |
    docker compose exec -T postgres psql `
        -v ON_ERROR_STOP=1 `
        -U $databaseUser `
        -d $databaseName

if ($LASTEXITCODE -ne 0) {
    throw 'La prueba SQL fallo.'
}
