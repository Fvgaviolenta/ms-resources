package cl.catastrofescl.resources.service;

import cl.catastrofescl.resources.entity.EventoProcesado;
import cl.catastrofescl.resources.entity.TipoMovimiento;
import cl.catastrofescl.resources.event.ItemMovimientoEventoDto;
import cl.catastrofescl.resources.event.TransferenciaEstadoCambiadaEvento;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicioProcesamientoTransferenciaEstadoTest {

    @Mock
    private RepositorioEventosProcesados repositorioEventosProcesados;
    @Mock
    private ServicioInventario servicioInventario;
    @Mock
    private ObjectProvider<StringRedisTemplate> redisPlantilla;

    @InjectMocks
    private ServicioProcesamientoTransferenciaEstado servicio;

    @Test
    void estadoNoRecibidaNoAplicaMovimiento() {
        servicio.procesar(TransferenciaEstadoCambiadaEvento.builder()
                .eventoId(UUID.randomUUID())
                .estadoNuevo("EN_TRANSITO")
                .build());

        verify(repositorioEventosProcesados, never()).existsByEventoId(any());
        verify(servicioInventario, never())
                .registrarMovimientoInterno(any(), any(), any(), anyLong(), any());
    }

    @Test
    void recibidaIdempotenteNoAplicaMovimiento() {
        UUID eventoId = UUID.randomUUID();
        when(repositorioEventosProcesados.existsByEventoId(eventoId)).thenReturn(true);

        servicio.procesar(TransferenciaEstadoCambiadaEvento.builder()
                .eventoId(eventoId)
                .estadoNuevo("RECIBIDA")
                .build());

        verify(servicioInventario, never())
                .registrarMovimientoInterno(any(), any(), any(), anyLong(), any());
    }

    @Test
    void recibidaEntreCentrosAplicaEgresoEIngreso() {
        UUID eventoId = UUID.randomUUID();
        UUID origen = UUID.randomUUID();
        UUID destino = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        when(repositorioEventosProcesados.existsByEventoId(eventoId)).thenReturn(false);
        when(repositorioEventosProcesados.save(any(EventoProcesado.class))).thenAnswer(inv -> inv.getArgument(0));

        servicio.procesar(TransferenciaEstadoCambiadaEvento.builder()
                .eventoId(eventoId)
                .estadoNuevo("RECIBIDA")
                .tipoDestino("CENTRO")
                .centroOrigenId(origen)
                .centroDestinoId(destino)
                .items(List.of(ItemMovimientoEventoDto.builder().itemId(itemId).cantidad(4L).build()))
                .build());

        verify(servicioInventario).registrarMovimientoInterno(
                eq(origen), eq(itemId), eq(TipoMovimiento.EGRESO), eq(4L), eq(null));
        verify(servicioInventario).registrarMovimientoInterno(
                eq(destino), eq(itemId), eq(TipoMovimiento.INGRESO), eq(4L), eq(null));
    }

    @Test
    void recibidaHaciaComunidadAplicaSoloEgreso() {
        UUID eventoId = UUID.randomUUID();
        UUID origen = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        when(repositorioEventosProcesados.existsByEventoId(eventoId)).thenReturn(false);
        when(repositorioEventosProcesados.save(any(EventoProcesado.class))).thenAnswer(inv -> inv.getArgument(0));

        servicio.procesar(TransferenciaEstadoCambiadaEvento.builder()
                .eventoId(eventoId)
                .estadoNuevo("RECIBIDA")
                .tipoDestino("COMUNIDAD")
                .centroOrigenId(origen)
                .centroDestinoId(null)
                .items(List.of(ItemMovimientoEventoDto.builder().itemId(itemId).cantidad(9L).build()))
                .build());

        verify(servicioInventario).registrarMovimientoInterno(
                eq(origen), eq(itemId), eq(TipoMovimiento.EGRESO), eq(9L), eq(null));
        verify(servicioInventario, times(1))
                .registrarMovimientoInterno(any(), any(), any(), anyLong(), any());
    }
}
