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

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "inventario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventario {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "centro_id", nullable = false)
    private UUID centroId;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false, length = 50)
    private CategoriaInventario categoria;

    @Column(name = "stock_actual", nullable = false)
    private int stockActual;

    @Column(name = "umbral_minimo", nullable = false)
    private int umbralMinimo;

    @Column(name = "umbral_optimo", nullable = false)
    private int umbralOptimo;

    @Column(name = "umbral_maximo", nullable = false)
    private int umbralMaximo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_criticidad", nullable = false, length = 30)
    private EstadoCriticidad estadoCriticidad;

    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (actualizadoEn == null) {
            actualizadoEn = OffsetDateTime.now();
        }
        if (estadoCriticidad == null) {
            estadoCriticidad = EstadoCriticidad.AGOTADO;
        }
    }

    @PreUpdate
    public void preUpdate() {
        actualizadoEn = OffsetDateTime.now();
    }
}
