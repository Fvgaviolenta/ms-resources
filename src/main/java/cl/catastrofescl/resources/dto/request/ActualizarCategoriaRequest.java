package cl.catastrofescl.resources.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record ActualizarCategoriaRequest(
        @Size(max = 100) String nombre,
        Boolean activo,
        @Min(0) Integer orden
) {
}
