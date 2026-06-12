package cl.catastrofescl.resources.dto.request;

import cl.catastrofescl.resources.entity.CategoriaInventario;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ActualizarUmbralesInventarioRequest(
        @NotNull CategoriaInventario categoria,
        @NotNull @Min(0) Integer umbralMinimo,
        @NotNull @Min(0) Integer umbralOptimo,
        @NotNull @Min(0) Integer umbralMaximo
) {
}
