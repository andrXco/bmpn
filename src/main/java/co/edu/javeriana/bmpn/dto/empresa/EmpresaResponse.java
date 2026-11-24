package co.edu.javeriana.bmpn.dto.empresa;

import java.time.Instant;

import co.edu.javeriana.bmpn.dto.usuario.UsuarioResponse;
public class EmpresaResponse {

    private Long id;
    private String nit;
    private String nombre;
    private String correoContacto;
    private Instant fechaCreacion;
    private boolean activo;
    private UsuarioResponse administradorInicial;

    public EmpresaResponse() {
    }

    public EmpresaResponse(
            Long id,
            String nit,
            String nombre,
            String correoContacto,
            Instant fechaCreacion,
            boolean activo,
            UsuarioResponse administradorInicial) {
        this.id = id;
        this.nit = nit;
        this.nombre = nombre;
        this.correoContacto = correoContacto;
        this.fechaCreacion = fechaCreacion;
        this.activo = activo;
        this.administradorInicial = administradorInicial;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreoContacto() {
        return correoContacto;
    }

    public void setCorreoContacto(String correoContacto) {
        this.correoContacto = correoContacto;
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

    public UsuarioResponse getAdministradorInicial() {
        return administradorInicial;
    }

    public void setAdministradorInicial(UsuarioResponse administradorInicial) {
        this.administradorInicial = administradorInicial;
    }
}
