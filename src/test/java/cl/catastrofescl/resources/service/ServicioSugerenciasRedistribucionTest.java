package cl.catastrofescl.resources.service;

import cl.catastrofescl.resources.entity.CategoriaInventario;
import cl.catastrofescl.resources.entity.EstadoCriticidad;
import cl.catastrofescl.resources.entity.Inventario;
import cl.catastrofescl.resources.repository.RepositorioCentros;
import cl.catastrofescl.resources.repository.RepositorioInventario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

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

    @InjectMocks
    private ServicioSugerenciasRedistribucion servicio;

    @Test
    void sinDemandaNiOfertaDevuelveListaVacia() {
        when(repositorioInventario.buscarPorCriticidad(eq(CategoriaInventario.ALIMENTOS), any()))
                .thenReturn(List.of());

        assertThat(servicio.sugerir(CategoriaInventario.ALIMENTOS, 5)).isEmpty();
    }

    @Test
    void soloDemandaSinOfertaDevuelveListaVacia() {
        when(repositorioInventario.buscarPorCriticidad(eq(CategoriaInventario.ALIMENTOS),
                eq(List.of(EstadoCriticidad.AGOTADO, EstadoCriticidad.CRITICO))))
                .thenReturn(List.of(Inventario.builder()
                        .centroId(java.util.UUID.randomUUID())
                        .categoria(CategoriaInventario.ALIMENTOS)
                        .stockActual(0)
                        .umbralOptimo(50)
                        .estadoCriticidad(EstadoCriticidad.AGOTADO)
                        .build()));
        when(repositorioInventario.buscarPorCriticidad(eq(CategoriaInventario.ALIMENTOS),
                eq(List.of(EstadoCriticidad.ABUNDANTE, EstadoCriticidad.SOBRESTOCK))))
                .thenReturn(List.of());

        assertThat(servicio.sugerir(CategoriaInventario.ALIMENTOS, 5)).isEmpty();
    }
}
