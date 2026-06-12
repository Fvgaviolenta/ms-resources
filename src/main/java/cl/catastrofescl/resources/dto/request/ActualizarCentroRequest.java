package cl.catastrofescl.resources.dto.request;

import cl.catastrofescl.resources.entity.EstadoCentro;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ActualizarCentroRequest(
        @Size(max = 200) String nombre,
        @Size(max = 500) String direccion,
        @Size(max = 100) String region,
        @Size(max = 100) String comuna,
        @Positive Integer capacidad,
        @Size(max = 200) String horario,
        EstadoCentro estado
) {
}
