package co.edu.javeriana.bmpn.dto.usuario;

import java.time.Instant;

import co.edu.javeriana.bmpn.entity.RolAcceso;

public class UsuarioResponse {

    private Long id;
    private Long empresaId;
    private String email;
    private String nombre;
    private String apellido;
    private RolAcceso rolAcceso;
    private Instant fechaCreacion;
    private boolean activo;

    public UsuarioResponse() {
    }

    public UsuarioResponse(
            Long id,
            Long empresaId,
            String email,
            String nombre,
            String apellido,
            RolAcceso rolAcceso,
            Instant fechaCreacion,
            boolean activo) {
        this.id = id;
        this.empresaId = empresaId;
        this.email = email;
        this.nombre = nombre;
        this.apellido = apellido;
        this.rolAcceso = rolAcceso;
        this.fechaCreacion = fechaCreacion;
        this.activo = activo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public RolAcceso getRolAcceso() {
        return rolAcceso;
    }

    public void setRolAcceso(RolAcceso rolAcceso) {
        this.rolAcceso = rolAcceso;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Instant fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
