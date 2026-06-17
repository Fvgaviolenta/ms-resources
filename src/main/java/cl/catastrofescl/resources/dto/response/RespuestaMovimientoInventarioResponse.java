package cl.catastrofescl.resources.dto.response;

import cl.catastrofescl.resources.entity.EstadoCriticidad;
import cl.catastrofescl.resources.entity.TipoMovimiento;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RespuestaMovimientoInventarioResponse(
        UUID itemCatalogoId,
        String itemNombre,
        String codigoCategoria,
        TipoMovimiento tipoMovimiento,
        long cantidad,
        long stockAnterior,
        long stockActual,
        EstadoCriticidad estadoCriticidad,
        OffsetDateTime actualizadoEn
) {
}
