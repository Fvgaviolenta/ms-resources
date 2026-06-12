package cl.catastrofescl.resources.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AsignarOperadorCentroRequest(
        @NotNull UUID usuarioId
) {
}
