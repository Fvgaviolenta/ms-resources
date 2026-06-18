package cl.catastrofescl.resources.service;

import cl.catastrofescl.resources.entity.EstadoCriticidad;
import cl.catastrofescl.resources.entity.Inventario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ServicioInventarioTest {

    @Mock
    private cl.catastrofescl.resources.repository.RepositorioInventario repositorioInventario;
    @Mock
    private cl.catastrofescl.resources.repository.RepositorioMovimientosInventario repositorioMovimientosInventario;
    @Mock
    private cl.catastrofescl.resources.repository.RepositorioCentros repositorioCentros;
    @Mock
    private cl.catastrofescl.resources.repository.RepositorioCatalogoItems repositorioCatalogoItems;
    @Mock
    private cl.catastrofescl.resources.repository.RepositorioCategorias repositorioCategorias;
    @Mock
    private PublicadorEventos publicadorEventos;
    @Mock
    private cl.catastrofescl.resources.seguridad.ContextoUsuario contextoUsuario;

    private ServicioInventario servicio;

    @BeforeEach
    void init() {
        servicio = new ServicioInventario(
                repositorioInventario,
                repositorioMovimientosInventario,
                repositorioCentros,
                repositorioCatalogoItems,
                repositorioCategorias,
                publicadorEventos,
                contextoUsuario
        );
    }

    @Test
    void calcularCriticidadAgotadoCuandoStockCero() {
        Inventario inventario = Inventario.builder()
                .stockActual(0L)
                .umbralMinimo(10L)
                .umbralOptimo(50L)
                .umbralMaximo(200L)
                .build();
        assertThat(servicio.calcularCriticidad(inventario)).isEqualTo(EstadoCriticidad.AGOTADO);
    }

    @Test
    void calcularCriticidadCriticoCuandoStockBajoMinimo() {
        Inventario inventario = Inventario.builder()
                .stockActual(5L)
                .umbralMinimo(10L)
                .umbralOptimo(50L)
                .umbralMaximo(200L)
                .build();
        assertThat(servicio.calcularCriticidad(inventario)).isEqualTo(EstadoCriticidad.CRITICO);
    }

    @Test
    void calcularCriticidadSobrestockCuandoSuperaMaximo() {
        Inventario inventario = Inventario.builder()
                .stockActual(250L)
                .umbralMinimo(10L)
                .umbralOptimo(50L)
                .umbralMaximo(200L)
                .build();
        assertThat(servicio.calcularCriticidad(inventario)).isEqualTo(EstadoCriticidad.SOBRESTOCK);
    }
}
