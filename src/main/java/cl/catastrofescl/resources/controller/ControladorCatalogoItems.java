package cl.catastrofescl.resources.controller;

import cl.catastrofescl.resources.dto.request.CrearItemCatalogoRequest;
import cl.catastrofescl.resources.dto.response.ItemCatalogoResponse;
import cl.catastrofescl.resources.service.ServicioCatalogoItems;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Tag(name = "Catalogo", description = "Catalogo de items de referencia por categoria")
@RestController
@RequestMapping("/catalogo/items")
@RequiredArgsConstructor
public class ControladorCatalogoItems {

    private final ServicioCatalogoItems servicioCatalogoItems;

    @Operation(summary = "Lista items activos del catalogo", description = "Publico. Cache Redis 300s.")
    @GetMapping
    public List<ItemCatalogoResponse> listar(@RequestParam(required = false) UUID categoriaId) {
        if (categoriaId != null) {
            return servicioCatalogoItems.listarPorCategoriaId(categoriaId);
        }
        return servicioCatalogoItems.listarActivos();
    }

    @Operation(summary = "Crea un item en el catalogo", description = "Solo ADMINISTRADOR. Inicializa filas de inventario en centros.")
    @PostMapping
    @PreAuthorize("hasAuthority('CATALOGO_GESTIONAR')")
    public ResponseEntity<ItemCatalogoResponse> crear(@Valid @RequestBody CrearItemCatalogoRequest solicitud) {
        ItemCatalogoResponse creado = servicioCatalogoItems.crear(solicitud);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creado.id())
                .toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }
}
