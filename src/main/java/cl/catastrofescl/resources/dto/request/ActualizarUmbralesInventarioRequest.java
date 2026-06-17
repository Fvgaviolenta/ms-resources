package cl.catastrofescl.resources.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ActualizarUmbralesInventarioRequest(
        @NotNull UUID itemCatalogoId,
        @NotNull @Min(0) Long umbralMinimo,
        @NotNull @Min(0) Long umbralOptimo,
        @NotNull @Min(0) Long umbralMaximo
) {
}
