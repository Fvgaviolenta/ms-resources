package cl.catastrofescl.resources.dto.request;

import cl.catastrofescl.resources.entity.CategoriaInventario;
import cl.catastrofescl.resources.entity.TipoMovimiento;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SolicitudMovimientoInventarioRequest(
        @NotNull CategoriaInventario categoria,
        @NotNull TipoMovimiento tipoMovimiento,
        @NotNull @Min(1) Integer cantidad
) {
}
