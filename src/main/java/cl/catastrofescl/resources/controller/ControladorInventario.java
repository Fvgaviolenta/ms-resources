package cl.catastrofescl.resources.controller;

import cl.catastrofescl.resources.dto.response.KpisInventarioResponse;
import cl.catastrofescl.resources.dto.response.SugerenciaRedistribucionResponse;
import cl.catastrofescl.resources.entity.CategoriaInventario;
import cl.catastrofescl.resources.service.ServicioKpis;
import cl.catastrofescl.resources.service.ServicioSugerenciasRedistribucion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Inventario", description = "KPIs y sugerencias de redistribucion")
@RestController
@RequestMapping("/inventario")
@RequiredArgsConstructor
@Validated
public class ControladorInventario {

    private final ServicioSugerenciasRedistribucion servicioSugerencias;
    private final ServicioKpis servicioKpis;

    @Operation(summary = "Sugerencias de redistribucion entre centros",
            description = "Empareja centros con sobrestock/abundante hacia centros criticos/agotados por categoria.")
    @GetMapping("/sugerencias")
    @PreAuthorize("hasAuthority('INVENTARIO_SUGERENCIAS')")
    public List<SugerenciaRedistribucionResponse> sugerencias(
            @RequestParam CategoriaInventario categoria,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limite) {
        return servicioSugerencias.sugerir(categoria, limite);
    }

    @Operation(summary = "KPIs agregados de inventario", description = "Cache Redis 30s.")
    @GetMapping("/kpis")
    @PreAuthorize("isAuthenticated()")
    public KpisInventarioResponse kpis() {
        return servicioKpis.obtenerKpis();
    }
}
