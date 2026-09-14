package co.edu.javeriana.bmpn.dto.autenticacion;

import java.io.Serializable;

import co.edu.javeriana.bmpn.entity.RolAcceso;
public class SesionUsuarioResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long usuarioId;
    private Long empresaId;
    private String email;
    private String nombreCompleto;
    private RolAcceso rolAcceso;

    public SesionUsuarioResponse() {
    }

    public SesionUsuarioResponse(
            Long usuarioId,
            Long empresaId,
            String email,
            String nombreCompleto,
            RolAcceso rolAcceso) {
        this.usuarioId = usuarioId;
        this.empresaId = empresaId;
        this.email = email;
        this.nombreCompleto = nombreCompleto;
        this.rolAcceso = rolAcceso;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public RolAcceso getRolAcceso() {
        return rolAcceso;
    }

    public void setRolAcceso(RolAcceso rolAcceso) {
        this.rolAcceso = rolAcceso;
    }
}
