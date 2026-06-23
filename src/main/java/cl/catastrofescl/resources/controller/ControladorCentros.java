package cl.catastrofescl.resources.controller;

import cl.catastrofescl.resources.dto.request.AsignarOperadorCentroRequest;
import cl.catastrofescl.resources.dto.request.ActualizarCentroRequest;
import cl.catastrofescl.resources.dto.request.ActualizarUmbralesInventarioRequest;
import cl.catastrofescl.resources.dto.request.CrearCentroRequest;
import cl.catastrofescl.resources.dto.request.SolicitudMovimientoInventarioRequest;
import cl.catastrofescl.resources.dto.response.CentroResponse;
import cl.catastrofescl.resources.dto.response.ColeccionMapaCentrosResponse;
import cl.catastrofescl.resources.dto.response.InventarioItemResponse;
import cl.catastrofescl.resources.dto.response.OperadorCentroResponse;
import cl.catastrofescl.resources.dto.response.ResumenInventarioCategoriaResponse;
import cl.catastrofescl.resources.dto.response.RespuestaMovimientoInventarioResponse;
import cl.catastrofescl.resources.service.ServicioCentros;
import cl.catastrofescl.resources.service.ServicioInventario;
import cl.catastrofescl.resources.service.ServicioMapData;
import cl.catastrofescl.resources.service.ServicioOperadoresCentro;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Tag(name = "Centros", description = "Centros de acopio e inventario por item")
@RestController
@RequestMapping("/centros")
@RequiredArgsConstructor
@Validated
public class ControladorCentros {

    private final ServicioCentros servicioCentros;
    private final ServicioInventario servicioInventario;
    private final ServicioMapData servicioMapData;
    private final ServicioOperadoresCentro servicioOperadoresCentro;

    @Operation(summary = "Crea un centro de acopio independiente")
    @PostMapping
    @PreAuthorize("hasAuthority('CENTRO_CREAR')")
    public ResponseEntity<CentroResponse> crear(@Valid @RequestBody CrearCentroRequest solicitud) {
        CentroResponse creado = servicioCentros.crear(solicitud);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creado.id())
                .toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    @Operation(summary = "Lista centros paginados", description = "Publico. Filtro opcional por emergenciaId.")
    @GetMapping
    public Page<CentroResponse> listar(
            @RequestParam(required = false) UUID emergenciaId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return servicioCentros.listar(emergenciaId, page, size);
    }

    @Operation(summary = "Lista centros cercanos", description = "Publico. PostGIS ST_DWithin.")
    @GetMapping("/cercanos")
    public Page<CentroResponse> listarCercanos(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "5000") @Positive double radioMetros,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return servicioCentros.listarCercanos(lat, lng, radioMetros, page, size);
    }

    @Operation(summary = "Datos optimizados para mapa Leaflet",
            description = "Endpoint PUBLICO. FeatureCollection con criticidad maxima por centro. Cache Redis 60s.")
    @GetMapping("/map-data")
    public ColeccionMapaCentrosResponse mapData() {
        return servicioMapData.obtenerDatosMapa();
    }

    @Operation(summary = "Obtiene detalle de un centro")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public CentroResponse obtener(@PathVariable UUID id) {
        return servicioCentros.obtener(id);
    }

    @Operation(summary = "Actualiza estado/capacidad de un centro")
    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('CENTRO_GESTIONAR')")
    public CentroResponse actualizar(@PathVariable UUID id,
                                     @Valid @RequestBody ActualizarCentroRequest solicitud) {
        return servicioCentros.actualizar(id, solicitud);
    }

    @Operation(summary = "Borrado logico de un centro", description = "Marca el centro como CERRADO; conserva el historial.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CENTRO_GESTIONAR')")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        servicioCentros.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Inventario por item del centro")
    @GetMapping("/{id}/inventario")
    @PreAuthorize("isAuthenticated()")
    public List<InventarioItemResponse> listarInventario(@PathVariable UUID id) {
        return servicioInventario.listarPorCentro(id);
    }

    @Operation(summary = "Resumen agregado de inventario por categoria")
    @GetMapping("/{id}/inventario/resumen-categorias")
    @PreAuthorize("isAuthenticated()")
    public List<ResumenInventarioCategoriaResponse> resumenInventarioPorCategoria(@PathVariable UUID id) {
        return servicioInventario.resumenPorCategoria(id);
    }

    @Operation(summary = "Registra ingreso o egreso por item de catalogo")
    @PostMapping("/{id}/inventario/movimientos")
    @PreAuthorize("hasAuthority('INVENTARIO_GESTIONAR')")
    public RespuestaMovimientoInventarioResponse registrarMovimiento(
            @PathVariable UUID id,
            @Valid @RequestBody SolicitudMovimientoInventarioRequest solicitud) {
        return servicioInventario.registrarMovimiento(id, solicitud);
    }

    @Operation(summary = "Actualiza umbrales de criticidad por item", description = "Solo ADMINISTRADOR.")
    @PatchMapping("/{id}/inventario/umbrales")
    @PreAuthorize("hasAuthority('INVENTARIO_UMBRALES')")
    public InventarioItemResponse actualizarUmbrales(
            @PathVariable UUID id,
            @Valid @RequestBody ActualizarUmbralesInventarioRequest solicitud) {
        return servicioInventario.actualizarUmbrales(id, solicitud);
    }

    @Operation(summary = "Asigna un operador al centro", description = "Requiere permiso CENTRO_ASIGNAR_OPERADOR (Autoridad).")
    @PostMapping("/{id}/operadores")
    @PreAuthorize("hasAuthority('CENTRO_ASIGNAR_OPERADOR')")
    public OperadorCentroResponse asignarOperador(
            @PathVariable UUID id,
            @Valid @RequestBody AsignarOperadorCentroRequest solicitud) {
        return servicioOperadoresCentro.asignar(id, solicitud);
    }

    @Operation(summary = "Lista operadores asignados al centro")
    @GetMapping("/{id}/operadores")
    @PreAuthorize("hasAuthority('CENTRO_GESTIONAR')")
    public List<OperadorCentroResponse> listarOperadores(@PathVariable UUID id) {
        return servicioOperadoresCentro.listarPorCentro(id);
    }
}
