package co.edu.javeriana.bmpn.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

// HU-25: cada dato que viaja en el mensaje, con su tipo
@Entity
@Table(name = "campo_mensaje")
@Getter
@NoArgsConstructor(access = PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CampoMensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mensaje_id", nullable = false)
    private Mensaje mensaje;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "tipo_dato", nullable = false, length = 50)
    private String tipoDato;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false)
    private boolean obligatorio;

    @Column(nullable = false)
    private boolean activo;

    public CampoMensaje(String nombre, String tipoDato, String descripcion, boolean obligatorio) {
        this.nombre = nombre;
        this.tipoDato = tipoDato;
        this.descripcion = descripcion;
        this.obligatorio = obligatorio;
        this.activo = true;
    }

    void asignarMensaje(Mensaje mensaje) {
        this.mensaje = mensaje;
    }
}
