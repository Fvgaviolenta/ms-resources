package cl.catastrofescl.resources.service;

import cl.catastrofescl.resources.dto.response.SugerenciaRedistribucionResponse;
import cl.catastrofescl.resources.entity.CategoriaInventario;
import cl.catastrofescl.resources.entity.Centro;
import cl.catastrofescl.resources.entity.EstadoCriticidad;
import cl.catastrofescl.resources.entity.Inventario;
import cl.catastrofescl.resources.entity.ItemCatalogo;
import cl.catastrofescl.resources.repository.RepositorioCatalogoItems;
import cl.catastrofescl.resources.repository.RepositorioCategorias;
import cl.catastrofescl.resources.repository.RepositorioCentros;
import cl.catastrofescl.resources.repository.RepositorioInventario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServicioSugerenciasRedistribucion {

    private static final List<EstadoCriticidad> ESTADOS_DEMANDA =
            List.of(EstadoCriticidad.AGOTADO, EstadoCriticidad.CRITICO);
    private static final List<EstadoCriticidad> ESTADOS_OFERTA =
            List.of(EstadoCriticidad.ABUNDANTE, EstadoCriticidad.SOBRESTOCK);

    private final RepositorioInventario repositorioInventario;
    private final RepositorioCentros repositorioCentros;
    private final RepositorioCatalogoItems repositorioCatalogoItems;
    private final RepositorioCategorias repositorioCategorias;

    @Transactional(readOnly = true)
    public List<SugerenciaRedistribucionResponse> sugerir(CategoriaInventario categoria, int limite) {
        String codigoCategoria = categoria != null ? categoria.name() : null;
        List<Inventario> demandantes = repositorioInventario.buscarPorCriticidad(codigoCategoria, ESTADOS_DEMANDA);
        List<Inventario> oferentes = repositorioInventario.buscarPorCriticidad(codigoCategoria, ESTADOS_OFERTA);

        if (demandantes.isEmpty() || oferentes.isEmpty()) {
            return List.of();
        }

        Map<UUID, ItemCatalogo> items = cargarItems(demandantes, oferentes);
        Map<UUID, Centro> centros = cargarCentros(demandantes, oferentes);
        Map<UUID, String> codigosCategoriaPorItem = repositorioCategorias.findAll().stream()
                .collect(Collectors.toMap(c -> c.getId(), c -> c.getCodigo()));
        List<SugerenciaRedistribucionResponse> sugerencias = new ArrayList<>();

        for (Inventario demanda : demandantes) {
            SugerenciaRedistribucionResponse mejor = oferentes.stream()
                    .filter(oferta -> oferta.getItemCatalogoId().equals(demanda.getItemCatalogoId()))
                    .filter(oferta -> oferta.getCentroId() != null
                            && !oferta.getCentroId().equals(demanda.getCentroId()))
                    .map(oferta -> construirSugerencia(demanda, oferta, centros, items, categoria, codigosCategoriaPorItem))
                    .min(Comparator.comparingDouble(SugerenciaRedistribucionResponse::distanciaMetros))
                    .orElse(null);

            if (mejor != null) {
                sugerencias.add(mejor);
            }
        }

        return sugerencias.stream()
                .sorted(Comparator.comparingDouble(SugerenciaRedistribucionResponse::distanciaMetros))
                .limit(limite)
                .toList();
    }

    private Map<UUID, ItemCatalogo> cargarItems(List<Inventario> demandantes, List<Inventario> oferentes) {
        List<UUID> ids = new ArrayList<>();
        demandantes.forEach(i -> ids.add(i.getItemCatalogoId()));
        oferentes.forEach(i -> ids.add(i.getItemCatalogoId()));
        return repositorioCatalogoItems.findByIdIn(ids.stream().distinct().toList()).stream()
                .collect(Collectors.toMap(ItemCatalogo::getId, Function.identity()));
    }

    private Map<UUID, Centro> cargarCentros(List<Inventario> demandantes, List<Inventario> oferentes) {
        List<UUID> ids = new ArrayList<>();
        demandantes.forEach(i -> ids.add(i.getCentroId()));
        oferentes.forEach(i -> ids.add(i.getCentroId()));
        return repositorioCentros.findAllById(ids.stream().distinct().toList()).stream()
                .collect(Collectors.toMap(Centro::getId, Function.identity()));
    }

    private SugerenciaRedistribucionResponse construirSugerencia(
            Inventario demanda,
            Inventario oferta,
            Map<UUID, Centro> centros,
            Map<UUID, ItemCatalogo> items,
            CategoriaInventario categoriaFiltro,
            Map<UUID, String> codigosCategoriaPorItem) {
        Centro centroOrigen = centros.get(oferta.getCentroId());
        Centro centroDestino = centros.get(demanda.getCentroId());
        ItemCatalogo item = items.get(demanda.getItemCatalogoId());
        Double distanciaMetros = repositorioCentros.distanciaMetrosEntreCentros(
                oferta.getCentroId(), demanda.getCentroId());

        long deficit = Math.max(0L, demanda.getUmbralOptimo() - demanda.getStockActual());
        long excedente = Math.max(0L, oferta.getStockActual() - oferta.getUmbralOptimo());
        long cantidadSugerida = Math.min(deficit, excedente);

        CategoriaInventario categoria = categoriaFiltro != null
                ? categoriaFiltro
                : (item != null && codigosCategoriaPorItem.containsKey(item.getCategoriaId())
                ? ServicioInventario.aCategoriaInventario(codigosCategoriaPorItem.get(item.getCategoriaId()))
                : CategoriaInventario.ARTICULOS_VARIOS);

        return new SugerenciaRedistribucionResponse(
                categoria,
                demanda.getItemCatalogoId(),
                item != null ? item.getNombre() : "Item",
                oferta.getCentroId(),
                centroOrigen != null ? centroOrigen.getNombre() : "Centro origen",
                oferta.getStockActual(),
                oferta.getEstadoCriticidad(),
                demanda.getCentroId(),
                centroDestino != null ? centroDestino.getNombre() : "Centro destino",
                demanda.getStockActual(),
                demanda.getEstadoCriticidad(),
                distanciaMetros != null ? distanciaMetros : 0.0,
                cantidadSugerida
        );
    }
}
