package cl.catastrofescl.resources.service;

import cl.catastrofescl.resources.entity.CategoriaInventario;
import cl.catastrofescl.resources.entity.EstadoCriticidad;
import cl.catastrofescl.resources.entity.Inventario;
import cl.catastrofescl.resources.repository.RepositorioCatalogoItems;
import cl.catastrofescl.resources.repository.RepositorioCategorias;
import cl.catastrofescl.resources.repository.RepositorioCentros;
import cl.catastrofescl.resources.repository.RepositorioInventario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicioSugerenciasRedistribucionTest {

    @Mock
    private RepositorioInventario repositorioInventario;
    @Mock
    private RepositorioCentros repositorioCentros;
    @Mock
    private RepositorioCatalogoItems repositorioCatalogoItems;
    @Mock
    private RepositorioCategorias repositorioCategorias;

    @InjectMocks
    private ServicioSugerenciasRedistribucion servicio;

    @Test
    void sinDemandaNiOfertaDevuelveListaVacia() {
        when(repositorioInventario.buscarPorCriticidad(eq("ALIMENTOS"), any()))
                .thenReturn(List.of());

        assertThat(servicio.sugerir(CategoriaInventario.ALIMENTOS, 5)).isEmpty();
    }

    @Test
    void soloDemandaSinOfertaDevuelveListaVacia() {
        when(repositorioInventario.buscarPorCriticidad(eq("ALIMENTOS"),
                eq(List.of(EstadoCriticidad.AGOTADO, EstadoCriticidad.CRITICO))))
                .thenReturn(List.of(Inventario.builder()
                        .centroId(UUID.randomUUID())
                        .itemCatalogoId(UUID.randomUUID())
                        .stockActual(0L)
                        .umbralOptimo(50L)
                        .estadoCriticidad(EstadoCriticidad.AGOTADO)
                        .build()));
        when(repositorioInventario.buscarPorCriticidad(eq("ALIMENTOS"),
                eq(List.of(EstadoCriticidad.ABUNDANTE, EstadoCriticidad.SOBRESTOCK))))
                .thenReturn(List.of());

        assertThat(servicio.sugerir(CategoriaInventario.ALIMENTOS, 5)).isEmpty();
    }
}
