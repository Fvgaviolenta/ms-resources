package cl.catastrofescl.resources.controller;

import cl.catastrofescl.resources.dto.request.ActualizarCategoriaRequest;
import cl.catastrofescl.resources.dto.request.CrearCategoriaRequest;
import cl.catastrofescl.resources.dto.response.CategoriaResponse;
import cl.catastrofescl.resources.service.ServicioCategorias;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Tag(name = "Categorias", description = "Categorias maestras de inventario")
@RestController
@RequestMapping("/categorias")
@RequiredArgsConstructor
public class ControladorCategorias {

    private final ServicioCategorias servicioCategorias;

    @Operation(summary = "Lista categorias activas", description = "Publico.")
    @GetMapping
    public List<CategoriaResponse> listar() {
        return servicioCategorias.listarActivas();
    }

    @Operation(summary = "Crea una categoria", description = "Solo ADMINISTRADOR.")
    @PostMapping
    @PreAuthorize("hasAuthority('CATEGORIA_GESTIONAR')")
    public ResponseEntity<CategoriaResponse> crear(@Valid @RequestBody CrearCategoriaRequest solicitud) {
        CategoriaResponse creada = servicioCategorias.crear(solicitud);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creada.id())
                .toUri();
        return ResponseEntity.created(ubicacion).body(creada);
    }

    @Operation(summary = "Actualiza una categoria", description = "Solo ADMINISTRADOR.")
    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('CATEGORIA_GESTIONAR')")
    public CategoriaResponse actualizar(@PathVariable UUID id,
                                        @Valid @RequestBody ActualizarCategoriaRequest solicitud) {
        return servicioCategorias.actualizar(id, solicitud);
    }
}
