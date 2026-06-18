package cl.catastrofescl.resources.integracion;

import cl.catastrofescl.resources.event.CentroAsociadoEnEventoDto;
import cl.catastrofescl.resources.event.EmergenciaCreadaEvento;
import cl.catastrofescl.resources.service.ServicioProcesamientoEmergenciaCreada;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class InventarioIntegracionTest extends BaseIntegracionTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ServicioProcesamientoEmergenciaCreada servicioProcesamientoEmergenciaCreada;

    private MockMvc mvc() {
        return MockMvcBuilders.webAppContextSetup(wac)
                .addFilter(springSecurityFilterChain, "/*")
                .build();
    }

    private final UUID uidDev = UUID.randomUUID();

    @Test
    void crearCentroInicializaInventarioPorItemYPermiteIngreso() throws Exception {
        ObjectNode centro = objectMapper.createObjectNode();
        centro.put("nombre", "Centro Integracion IT");
        ObjectNode coordenadas = centro.putObject("coordenadas");
        coordenadas.put("longitud", -70.65);
        coordenadas.put("latitud", -33.43);
        centro.put("region", "Metropolitana");
        centro.put("capacidad", 300);

        MvcResult creado = mvc().perform(post("/centros")
                        .header("X-Dev-Firebase-Uid", uidDev.toString())
                        .header("X-Dev-Roles", "ADMINISTRADOR")
                        .header("X-Dev-Usuario-Id", uidDev.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(centro)))
                .andExpect(status().isCreated())
                .andReturn();

        String centroId = objectMapper.readTree(creado.getResponse().getContentAsString()).get("id").asText();

        mvc().perform(get("/centros/" + centroId + "/inventario")
                        .header("X-Dev-Firebase-Uid", uidDev.toString())
                        .header("X-Dev-Roles", "ADMINISTRADOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[0].itemCatalogoId", notNullValue()));

        MvcResult catalogo = mvc().perform(get("/catalogo/items"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode primerItem = objectMapper.readTree(catalogo.getResponse().getContentAsString()).get(0);
        String itemId = primerItem.get("id").asText();

        ObjectNode movimiento = objectMapper.createObjectNode();
        movimiento.put("itemCatalogoId", itemId);
        movimiento.put("tipoMovimiento", "INGRESO");
        movimiento.put("cantidad", 120);

        mvc().perform(post("/centros/" + centroId + "/inventario/movimientos")
                        .header("X-Dev-Firebase-Uid", uidDev.toString())
                        .header("X-Dev-Roles", "OPERADOR")
                        .header("X-Dev-Usuario-Id", uidDev.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(movimiento)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockActual", is(120)))
                .andExpect(jsonPath("$.itemCatalogoId", is(itemId)));

        mvc().perform(get("/centros/" + centroId + "/inventario/resumen-categorias")
                        .header("X-Dev-Firebase-Uid", uidDev.toString())
                        .header("X-Dev-Roles", "ADMINISTRADOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))));
    }

    @Test
    void procesarEmergenciaCreadaEsIdempotenteYCreaCentro() throws Exception {
        UUID emergenciaId = UUID.randomUUID();
        UUID eventoId = UUID.randomUUID();

        EmergenciaCreadaEvento evento = EmergenciaCreadaEvento.builder()
                .eventoId(eventoId)
                .ocurridoEn(OffsetDateTime.now())
                .correlacionId(emergenciaId.toString())
                .versionEvento("1.0")
                .fuente("ms-emergencies")
                .emergenciaId(emergenciaId)
                .region("Metropolitana")
                .declaradaPorUsuarioId(uidDev)
                .centrosAsociados(List.of(CentroAsociadoEnEventoDto.builder()
                        .nombre("Centro Cola IT")
                        .longitud(-70.66)
                        .latitud(-33.44)
                        .capacidadEstimada(200)
                        .build()))
                .build();

        servicioProcesamientoEmergenciaCreada.procesar(evento);
        servicioProcesamientoEmergenciaCreada.procesar(evento);

        mvc().perform(get("/centros")
                        .param("emergenciaId", emergenciaId.toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].nombre", is("Centro Cola IT")));
    }
}
