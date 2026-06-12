package cl.catastrofescl.resources.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "movimientos_inventario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoInventario {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "inventario_id", nullable = false)
    private UUID inventarioId;

    @Column(name = "centro_id", nullable = false)
    private UUID centroId;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false, length = 50)
    private CategoriaInventario categoria;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento", nullable = false, length = 20)
    private TipoMovimiento tipoMovimiento;

    @Column(name = "cantidad", nullable = false)
    private int cantidad;

    @Column(name = "stock_anterior", nullable = false)
    private int stockAnterior;

    @Column(name = "stock_posterior", nullable = false)
    private int stockPosterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_criticidad_posterior", nullable = false, length = 30)
    private EstadoCriticidad estadoCriticidadPosterior;

    @Column(name = "registrado_por_usuario_id")
    private UUID registradoPorUsuarioId;

    @Column(name = "registrado_en", nullable = false)
    private OffsetDateTime registradoEn;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (registradoEn == null) {
            registradoEn = OffsetDateTime.now();
        }
    }
}
