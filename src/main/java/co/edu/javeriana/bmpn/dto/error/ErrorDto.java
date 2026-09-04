package co.edu.javeriana.bmpn.dto.error;

import java.time.Instant;
import java.util.List;

public class ErrorDto {

    private String codigo;
    private String mensaje;
    private Instant fecha;
    private List<String> detalles;

    public ErrorDto() {
    }

    public ErrorDto(String codigo, String mensaje, Instant fecha, List<String> detalles) {
        this.codigo = codigo;
        this.mensaje = mensaje;
        this.fecha = fecha;
        this.detalles = detalles;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public Instant getFecha() {
        return fecha;
    }

    public void setFecha(Instant fecha) {
        this.fecha = fecha;
    }

    public List<String> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<String> detalles) {
        this.detalles = detalles;
    }
}
