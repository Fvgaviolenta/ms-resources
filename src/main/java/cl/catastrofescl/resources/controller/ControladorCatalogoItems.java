package cl.catastrofescl.resources.controller;

import cl.catastrofescl.resources.dto.response.ItemCatalogoResponse;
import cl.catastrofescl.resources.entity.CategoriaInventario;
import cl.catastrofescl.resources.service.ServicioCatalogoItems;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Catalogo", description = "Catalogo de items de referencia por categoria")
@RestController
@RequestMapping("/catalogo/items")
@RequiredArgsConstructor
public class ControladorCatalogoItems {

    private final ServicioCatalogoItems servicioCatalogoItems;

    @Operation(summary = "Lista items activos del catalogo", description = "Publico. Cache Redis 300s.")
    @GetMapping
    public List<ItemCatalogoResponse> listar(
            @RequestParam(required = false) CategoriaInventario categoria) {
        if (categoria != null) {
            return servicioCatalogoItems.listarPorCategoria(categoria);
        }
        return servicioCatalogoItems.listarActivos();
    }
}
