package cl.catastrofescl.resources.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CentroAsociadoEnEventoDto {

    private String nombre;
    private double longitud;
    private double latitud;
    private Integer capacidadEstimada;
}
