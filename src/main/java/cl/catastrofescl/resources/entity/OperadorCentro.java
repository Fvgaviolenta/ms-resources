package cl.catastrofescl.resources.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "operadores_centro")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperadorCentro {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "centro_id", nullable = false)
    private UUID centroId;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "asignado_por_usuario_id", nullable = false)
    private UUID asignadoPorUsuarioId;

    @Column(name = "asignado_en", nullable = false)
    private OffsetDateTime asignadoEn;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (asignadoEn == null) {
            asignadoEn = OffsetDateTime.now();
        }
    }
}
