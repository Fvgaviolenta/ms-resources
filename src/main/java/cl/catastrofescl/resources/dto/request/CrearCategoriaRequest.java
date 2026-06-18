package cl.catastrofescl.resources.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CrearCategoriaRequest(
        @NotBlank @Size(max = 50) String codigo,
        @NotBlank @Size(max = 100) String nombre,
        @Min(0) int orden
) {
}
