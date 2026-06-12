package cl.catastrofescl.resources.service;

import cl.catastrofescl.resources.dto.CoordenadaDto;
import cl.catastrofescl.resources.dto.request.CrearCentroRequest;
import cl.catastrofescl.resources.dto.response.CentroResponse;
import cl.catastrofescl.resources.entity.Centro;
import cl.catastrofescl.resources.entity.EstadoCentro;
import cl.catastrofescl.resources.repository.RepositorioCentros;
import cl.catastrofescl.resources.seguridad.ContextoUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicioCentrosTest {

    @Mock
    private RepositorioCentros repositorioCentros;
    @Mock
    private ServicioInventario servicioInventario;
    @Mock
    private ContextoUsuario contextoUsuario;

    private GeometriaMapper geometriaMapper;
    private MapeadorCentros mapeadorCentros;
    private ServicioCentros servicio;

    private final UUID usuarioId = UUID.randomUUID();

    @BeforeEach
    void init() {
        geometriaMapper = new GeometriaMapper();
        mapeadorCentros = new MapeadorCentros(geometriaMapper);
        servicio = new ServicioCentros(repositorioCentros, servicioInventario, geometriaMapper,
                mapeadorCentros, contextoUsuario);
    }

    @Test
    void crearCentroInicializaInventario() {
        when(contextoUsuario.usuarioIdActual()).thenReturn(usuarioId);
        when(repositorioCentros.save(any(Centro.class))).thenAnswer(inv -> {
            Centro c = inv.getArgument(0);
            c.setId(UUID.randomUUID());
            c.setCreadoEn(OffsetDateTime.now());
            c.setActualizadoEn(OffsetDateTime.now());
            return c;
        });

        CrearCentroRequest solicitud = new CrearCentroRequest(
                "Centro Norte",
                "Av. Principal 100",
                new CoordenadaDto(-70.65, -33.43),
                "Metropolitana",
                "Santiago",
                500,
                "08:00-20:00",
                EstadoCentro.ACTIVO
        );

        CentroResponse resp = servicio.crear(solicitud);

        assertThat(resp.nombre()).isEqualTo("Centro Norte");
        assertThat(resp.estado()).isEqualTo(EstadoCentro.ACTIVO);
        verify(servicioInventario).inicializarInventarioCentro(resp.id());
    }
}
