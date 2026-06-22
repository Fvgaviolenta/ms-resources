package cl.catastrofescl.resources.service;

import cl.catastrofescl.resources.dto.request.ActualizarUmbralesInventarioRequest;
import cl.catastrofescl.resources.entity.Categoria;
import cl.catastrofescl.resources.entity.Centro;
import cl.catastrofescl.resources.entity.EstadoCriticidad;
import cl.catastrofescl.resources.entity.Inventario;
import cl.catastrofescl.resources.entity.ItemCatalogo;
import cl.catastrofescl.resources.event.StockCriticoEvento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Test
    void actualizarUmbralesPublicaStockCriticoCuandoQuedaCritico() {
        UUID centroId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        UUID categoriaId = UUID.randomUUID();
        UUID emergenciaId = UUID.randomUUID();

        when(repositorioCentros.findById(centroId)).thenReturn(Optional.of(
                Centro.builder().id(centroId).emergenciaId(emergenciaId).build()));
        when(repositorioCatalogoItems.findById(itemId)).thenReturn(Optional.of(
                ItemCatalogo.builder().id(itemId).nombre("Agua").categoriaId(categoriaId)
                        .unidadMedida("L").activo(true).build()));
        when(repositorioCategorias.findById(categoriaId)).thenReturn(Optional.of(
                Categoria.builder().id(categoriaId).codigo("ALIMENTOS").nombre("Alimentos").build()));
        when(repositorioInventario.findByCentroIdAndItemCatalogoId(centroId, itemId)).thenReturn(Optional.of(
                Inventario.builder().id(UUID.randomUUID()).centroId(centroId).itemCatalogoId(itemId).stockActual(5L)
                        .umbralMinimo(2L).umbralOptimo(20L).umbralMaximo(100L)
                        .estadoCriticidad(EstadoCriticidad.NORMAL).build()));
        when(repositorioInventario.save(any(Inventario.class))).thenAnswer(inv -> inv.getArgument(0));

        servicio.actualizarUmbrales(centroId,
                new ActualizarUmbralesInventarioRequest(itemId, 10L, 50L, 200L));

        verify(publicadorEventos).publicar(any(StockCriticoEvento.class));
    }

    @Test
    void actualizarUmbralesNoPublicaCuandoQuedaNormal() {
        UUID centroId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        UUID categoriaId = UUID.randomUUID();

        when(repositorioCentros.findById(centroId)).thenReturn(Optional.of(
                Centro.builder().id(centroId).build()));
        when(repositorioCatalogoItems.findById(itemId)).thenReturn(Optional.of(
                ItemCatalogo.builder().id(itemId).nombre("Agua").categoriaId(categoriaId)
                        .unidadMedida("L").activo(true).build()));
        when(repositorioCategorias.findById(categoriaId)).thenReturn(Optional.of(
                Categoria.builder().id(categoriaId).codigo("ALIMENTOS").nombre("Alimentos").build()));
        when(repositorioInventario.findByCentroIdAndItemCatalogoId(centroId, itemId)).thenReturn(Optional.of(
                Inventario.builder().centroId(centroId).itemCatalogoId(itemId).stockActual(40L)
                        .umbralMinimo(2L).umbralOptimo(20L).umbralMaximo(100L)
                        .estadoCriticidad(EstadoCriticidad.NORMAL).build()));
        when(repositorioInventario.save(any(Inventario.class))).thenAnswer(inv -> inv.getArgument(0));

        servicio.actualizarUmbrales(centroId,
                new ActualizarUmbralesInventarioRequest(itemId, 10L, 30L, 200L));

        verify(publicadorEventos, never()).publicar(any(StockCriticoEvento.class));
    }
}
