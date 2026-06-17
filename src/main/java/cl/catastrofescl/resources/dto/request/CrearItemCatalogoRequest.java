package cl.catastrofescl.resources.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CrearItemCatalogoRequest(
        @NotBlank @Size(max = 200) String nombre,
        @NotNull UUID categoriaId,
        @Size(max = 500) String descripcion,
        @NotBlank @Size(max = 30) String unidadMedida
) {
}
