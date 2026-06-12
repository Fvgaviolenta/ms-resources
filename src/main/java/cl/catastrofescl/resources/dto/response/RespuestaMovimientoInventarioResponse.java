package cl.catastrofescl.resources.dto.response;

import cl.catastrofescl.resources.entity.CategoriaInventario;
import cl.catastrofescl.resources.entity.EstadoCriticidad;
import cl.catastrofescl.resources.entity.TipoMovimiento;

import java.time.OffsetDateTime;

public record RespuestaMovimientoInventarioResponse(
        CategoriaInventario categoria,
        TipoMovimiento tipoMovimiento,
        int cantidad,
        int stockAnterior,
        int stockActual,
        EstadoCriticidad estadoCriticidad,
        OffsetDateTime actualizadoEn
) {
}
