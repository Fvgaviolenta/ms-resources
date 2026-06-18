package cl.catastrofescl.resources.service;

import cl.catastrofescl.resources.dto.request.CrearItemCatalogoRequest;
import cl.catastrofescl.resources.dto.response.ItemCatalogoResponse;
import cl.catastrofescl.resources.entity.Categoria;
import cl.catastrofescl.resources.entity.ItemCatalogo;
import cl.catastrofescl.resources.exception.CategoriaNoEncontradaException;
import cl.catastrofescl.resources.repository.RepositorioCatalogoItems;
import cl.catastrofescl.resources.repository.RepositorioCategorias;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServicioCatalogoItems {

    public static final String CACHE_CATALOGO = "catalogo-items";

    private final RepositorioCatalogoItems repositorioCatalogoItems;
    private final RepositorioCategorias repositorioCategorias;
    private final @Lazy ServicioInventario servicioInventario;

    @Transactional(readOnly = true)
    public List<ItemCatalogoResponse> listarActivos() {
        return mapearLista(repositorioCatalogoItems.findByActivoTrueOrderByNombreAsc());
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_CATALOGO, key = "#categoriaId")
    public List<ItemCatalogoResponse> listarPorCategoriaId(UUID categoriaId) {
        return mapearLista(repositorioCatalogoItems.findByCategoriaIdAndActivoTrueOrderByNombreAsc(categoriaId));
    }

    @Transactional
    @CacheEvict(value = CACHE_CATALOGO, allEntries = true)
    public ItemCatalogoResponse crear(CrearItemCatalogoRequest solicitud) {
        Categoria categoria = repositorioCategorias.findById(solicitud.categoriaId())
                .orElseThrow(() -> new CategoriaNoEncontradaException(solicitud.categoriaId().toString()));

        ItemCatalogo item = ItemCatalogo.builder()
                .nombre(solicitud.nombre())
                .categoriaId(categoria.getId())
                .descripcion(solicitud.descripcion())
                .unidadMedida(solicitud.unidadMedida())
                .activo(true)
                .build();
        ItemCatalogo persistido = repositorioCatalogoItems.save(item);
        servicioInventario.inicializarFilaParaItemEnCentros(persistido.getId());
        return aResponse(persistido, categoria);
    }

    private List<ItemCatalogoResponse> mapearLista(List<ItemCatalogo> items) {
        if (items.isEmpty()) {
            return List.of();
        }
        Map<UUID, Categoria> categorias = repositorioCategorias.findAll().stream()
                .collect(Collectors.toMap(Categoria::getId, Function.identity()));
        return items.stream()
                .map(item -> aResponse(item, categorias.get(item.getCategoriaId())))
                .toList();
    }

    private ItemCatalogoResponse aResponse(ItemCatalogo item, Categoria categoria) {
        return new ItemCatalogoResponse(
                item.getId(),
                item.getNombre(),
                item.getCategoriaId(),
                categoria != null ? categoria.getCodigo() : "",
                categoria != null ? categoria.getNombre() : "",
                item.getDescripcion(),
                item.getUnidadMedida(),
                item.isActivo(),
                item.getCreadoEn()
        );
    }
}
