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
    private MapeadorCentros mapeadorCentros;
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
                mapeadorCentros,
                publicadorEventos,
                contextoUsuario
        );
    }

    @Test
    void calcularCriticidadAgotadoCuandoStockCero() {
        Inventario inventario = Inventario.builder()
                .stockActual(0)
                .umbralMinimo(10)
                .umbralOptimo(50)
                .umbralMaximo(200)
                .build();
        assertThat(servicio.calcularCriticidad(inventario)).isEqualTo(EstadoCriticidad.AGOTADO);
    }

    @Test
    void calcularCriticidadCriticoCuandoStockBajoMinimo() {
        Inventario inventario = Inventario.builder()
                .stockActual(5)
                .umbralMinimo(10)
                .umbralOptimo(50)
                .umbralMaximo(200)
                .build();
        assertThat(servicio.calcularCriticidad(inventario)).isEqualTo(EstadoCriticidad.CRITICO);
    }

    @Test
    void calcularCriticidadSobrestockCuandoSuperaMaximo() {
        Inventario inventario = Inventario.builder()
                .stockActual(250)
                .umbralMinimo(10)
                .umbralOptimo(50)
                .umbralMaximo(200)
                .build();
        assertThat(servicio.calcularCriticidad(inventario)).isEqualTo(EstadoCriticidad.SOBRESTOCK);
    }
}
