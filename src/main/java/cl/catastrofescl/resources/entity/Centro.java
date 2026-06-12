package cl.catastrofescl.resources.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "centros")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Centro {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "direccion", length = 500)
    private String direccion;

    @JdbcTypeCode(SqlTypes.GEOGRAPHY)
    @Column(name = "coordenadas", nullable = false, columnDefinition = "geography(Point,4326)")
    private Point coordenadas;

    @Column(name = "region", length = 100)
    private String region;

    @Column(name = "comuna", length = 100)
    private String comuna;

    @Column(name = "capacidad")
    private Integer capacidad;

    @Column(name = "horario", length = 200)
    private String horario;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoCentro estado;

    @Column(name = "emergencia_id")
    private UUID emergenciaId;

    @Column(name = "creado_por_usuario_id")
    private UUID creadoPorUsuarioId;

    @Column(name = "creado_en", nullable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        OffsetDateTime ahora = OffsetDateTime.now();
        if (creadoEn == null) {
            creadoEn = ahora;
        }
        if (actualizadoEn == null) {
            actualizadoEn = ahora;
        }
        if (estado == null) {
            estado = EstadoCentro.ACTIVO;
        }
    }

    @PreUpdate
    public void preUpdate() {
        actualizadoEn = OffsetDateTime.now();
    }
}
