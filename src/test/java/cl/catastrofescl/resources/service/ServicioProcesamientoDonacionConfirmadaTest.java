package cl.catastrofescl.resources.service;

import cl.catastrofescl.resources.entity.EventoProcesado;
import cl.catastrofescl.resources.entity.TipoMovimiento;
import cl.catastrofescl.resources.event.DonacionConfirmadaEvento;
import cl.catastrofescl.resources.event.ItemMovimientoEventoDto;
import cl.catastrofescl.resources.repository.RepositorioEventosProcesados;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicioProcesamientoDonacionConfirmadaTest {

    @Mock
    private RepositorioEventosProcesados repositorioEventosProcesados;
    @Mock
    private ServicioInventario servicioInventario;
    @Mock
    private ObjectProvider<StringRedisTemplate> redisPlantilla;

    @InjectMocks
    private ServicioProcesamientoDonacionConfirmada servicio;

    @Test
    void procesarIdempotenteNoAplicaIngreso() {
        UUID eventId = UUID.randomUUID();
        when(repositorioEventosProcesados.existsByEventoId(eventId)).thenReturn(true);

        servicio.procesar(DonacionConfirmadaEvento.builder()
                .eventId(eventId)
                .centroId(UUID.randomUUID())
                .build());

        verify(servicioInventario, never())
                .registrarMovimientoInterno(any(), any(), any(), anyLong(), any());
    }

    @Test
    void procesarSinEventIdSeIgnora() {
        servicio.procesar(DonacionConfirmadaEvento.builder().build());

        verify(repositorioEventosProcesados, never()).existsByEventoId(any());
        verify(servicioInventario, never())
                .registrarMovimientoInterno(any(), any(), any(), anyLong(), any());
    }

    @Test
    void procesarAplicaIngresoPorItem() {
        UUID eventId = UUID.randomUUID();
        UUID centroId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        UUID confirmadoPor = UUID.randomUUID();
        when(repositorioEventosProcesados.existsByEventoId(eventId)).thenReturn(false);
        when(repositorioEventosProcesados.save(any(EventoProcesado.class))).thenAnswer(inv -> inv.getArgument(0));

        servicio.procesar(DonacionConfirmadaEvento.builder()
                .eventId(eventId)
                .centroId(centroId)
                .confirmadoPorUsuarioId(confirmadoPor)
                .items(List.of(ItemMovimientoEventoDto.builder()
                        .itemId(itemId)
                        .cantidad(7L)
                        .build()))
                .build());

        verify(servicioInventario).registrarMovimientoInterno(
                eq(centroId), eq(itemId), eq(TipoMovimiento.INGRESO), eq(7L), eq(confirmadoPor));
    }
}
