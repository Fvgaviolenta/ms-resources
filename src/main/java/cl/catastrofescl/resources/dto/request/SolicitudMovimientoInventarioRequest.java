package cl.catastrofescl.resources.dto.request;

import cl.catastrofescl.resources.entity.TipoMovimiento;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SolicitudMovimientoInventarioRequest(
        @NotNull UUID itemCatalogoId,
        @NotNull TipoMovimiento tipoMovimiento,
        @NotNull @Min(1) Long cantidad
) {
}
