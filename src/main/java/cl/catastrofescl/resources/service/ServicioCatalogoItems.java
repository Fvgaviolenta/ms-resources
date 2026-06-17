package cl.catastrofescl.resources.service;

import cl.catastrofescl.resources.dto.response.ItemCatalogoResponse;
import cl.catastrofescl.resources.entity.CategoriaInventario;
import cl.catastrofescl.resources.entity.ItemCatalogo;
import cl.catastrofescl.resources.repository.RepositorioCatalogoItems;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServicioCatalogoItems {

    public static final String CACHE_CATALOGO = "catalogo-items";

    private final RepositorioCatalogoItems repositorioCatalogoItems;

    @Transactional(readOnly = true)
    public List<ItemCatalogoResponse> listarActivos() {
        return repositorioCatalogoItems.findByActivoTrueOrderByCategoriaAscNombreAsc().stream()
                .map(this::aResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ItemCatalogoResponse> listarPorCategoria(CategoriaInventario categoria) {
        return repositorioCatalogoItems.findByCategoriaAndActivoTrueOrderByNombreAsc(categoria).stream()
                .map(this::aResponse)
                .toList();
    }

    private ItemCatalogoResponse aResponse(ItemCatalogo item) {
        return new ItemCatalogoResponse(
                item.getId(),
                item.getNombre(),
                item.getCategoria(),
                item.getDescripcion(),
                item.getUnidadMedida(),
                item.isActivo(),
                item.getCreadoEn()
        );
    }
}
