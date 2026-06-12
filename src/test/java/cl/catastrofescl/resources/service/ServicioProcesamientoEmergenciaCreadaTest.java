package cl.catastrofescl.resources.service;

import cl.catastrofescl.resources.entity.EventoProcesado;
import cl.catastrofescl.resources.event.CentroAsociadoEnEventoDto;
import cl.catastrofescl.resources.event.EmergenciaCreadaEvento;
import cl.catastrofescl.resources.repository.RepositorioEventosProcesados;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicioProcesamientoEmergenciaCreadaTest {

    @Mock
    private RepositorioEventosProcesados repositorioEventosProcesados;
    @Mock
    private ServicioCentros servicioCentros;
    @Mock
    private ObjectProvider<org.springframework.data.redis.core.StringRedisTemplate> redisPlantilla;

    @InjectMocks
    private ServicioProcesamientoEmergenciaCreada servicio;

    @Test
    void procesarIdempotenteNoRecreaCentros() {
        UUID eventoId = UUID.randomUUID();
        when(repositorioEventosProcesados.existsByEventoId(eventoId)).thenReturn(true);

        servicio.procesar(EmergenciaCreadaEvento.builder()
                .eventoId(eventoId)
                .emergenciaId(UUID.randomUUID())
                .build());

        verify(servicioCentros, never()).crearDesdeEvento(any(), any(), any(), any());
    }

    @Test
    void procesarCreaCentrosDesdeEvento() {
        UUID eventoId = UUID.randomUUID();
        UUID emergenciaId = UUID.randomUUID();
        when(repositorioEventosProcesados.existsByEventoId(eventoId)).thenReturn(false);
        when(repositorioEventosProcesados.save(any(EventoProcesado.class))).thenAnswer(inv -> inv.getArgument(0));

        CentroAsociadoEnEventoDto centro = CentroAsociadoEnEventoDto.builder()
                .nombre("Centro Test")
                .longitud(-70.6)
                .latitud(-33.4)
                .capacidadEstimada(100)
                .build();

        servicio.procesar(EmergenciaCreadaEvento.builder()
                .eventoId(eventoId)
                .ocurridoEn(OffsetDateTime.now())
                .emergenciaId(emergenciaId)
                .region("Metropolitana")
                .centrosAsociados(List.of(centro))
                .build());

        verify(servicioCentros).crearDesdeEvento(emergenciaId, null, "Metropolitana", centro);
    }
}
