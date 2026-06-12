package cl.catastrofescl.resources.dto.request;

import cl.catastrofescl.resources.dto.CoordenadaDto;
import cl.catastrofescl.resources.entity.EstadoCentro;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CrearCentroRequest(
        @NotBlank @Size(max = 200) String nombre,
        @Size(max = 500) String direccion,
        @NotNull @Valid CoordenadaDto coordenadas,
        @Size(max = 100) String region,
        @Size(max = 100) String comuna,
        @Positive Integer capacidad,
        @Size(max = 200) String horario,
        EstadoCentro estado
) {
}
