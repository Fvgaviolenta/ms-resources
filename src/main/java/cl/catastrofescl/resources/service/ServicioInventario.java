package cl.catastrofescl.resources.service;

import cl.catastrofescl.resources.dto.request.ActualizarUmbralesInventarioRequest;
import cl.catastrofescl.resources.dto.request.SolicitudMovimientoInventarioRequest;
import cl.catastrofescl.resources.dto.response.InventarioItemResponse;
import cl.catastrofescl.resources.dto.response.ResumenInventarioCategoriaResponse;
import cl.catastrofescl.resources.dto.response.RespuestaMovimientoInventarioResponse;
import cl.catastrofescl.resources.entity.Categoria;
import cl.catastrofescl.resources.entity.CategoriaInventario;
import cl.catastrofescl.resources.entity.EstadoCriticidad;
import cl.catastrofescl.resources.entity.Inventario;
import cl.catastrofescl.resources.entity.ItemCatalogo;
import cl.catastrofescl.resources.entity.MovimientoInventario;
import cl.catastrofescl.resources.entity.TipoMovimiento;
import cl.catastrofescl.resources.event.MovimientoInventarioRegistradoEvento;
import cl.catastrofescl.resources.event.StockActualizadoEvento;
import cl.catastrofescl.resources.event.StockCriticoEvento;
import cl.catastrofescl.resources.exception.CentroNoEncontradoException;
import cl.catastrofescl.resources.exception.InventarioNoEncontradoException;
import cl.catastrofescl.resources.exception.ItemCatalogoNoEncontradoException;
import cl.catastrofescl.resources.exception.StockInsuficienteException;
import cl.catastrofescl.resources.exception.UmbralesInvalidosException;
import cl.catastrofescl.resources.repository.ProyeccionResumenInventarioCategoria;
import cl.catastrofescl.resources.repository.RepositorioCatalogoItems;
import cl.catastrofescl.resources.repository.RepositorioCategorias;
import cl.catastrofescl.resources.repository.RepositorioCentros;
import cl.catastrofescl.resources.repository.RepositorioInventario;
import cl.catastrofescl.resources.repository.RepositorioMovimientosInventario;
import cl.catastrofescl.resources.seguridad.ContextoUsuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServicioInventario {

    private static final long UMBRAL_MINIMO_DEFECTO = 10L;
    private static final long UMBRAL_OPTIMO_DEFECTO = 50L;
    private static final long UMBRAL_MAXIMO_DEFECTO = 200L;

    private final RepositorioInventario repositorioInventario;
    private final RepositorioMovimientosInventario repositorioMovimientosInventario;
    private final RepositorioCentros repositorioCentros;
    private final RepositorioCatalogoItems repositorioCatalogoItems;
    private final RepositorioCategorias repositorioCategorias;
    private final PublicadorEventos publicadorEventos;
    private final ContextoUsuario contextoUsuario;

    @Transactional
    public void inicializarInventarioCentro(UUID centroId) {
        List<ItemCatalogo> items = repositorioCatalogoItems.findByActivoTrueOrderByNombreAsc();
        for (ItemCatalogo item : items) {
            Inventario fila = Inventario.builder()
                    .centroId(centroId)
                    .itemCatalogoId(item.getId())
                    .stockActual(0L)
                    .umbralMinimo(UMBRAL_MINIMO_DEFECTO)
                    .umbralOptimo(UMBRAL_OPTIMO_DEFECTO)
                    .umbralMaximo(UMBRAL_MAXIMO_DEFECTO)
                    .estadoCriticidad(EstadoCriticidad.AGOTADO)
                    .build();
            repositorioInventario.save(fila);
        }
        log.debug("Inventario por item inicializado para centro {} ({} items)", centroId, items.size());
    }

    @Transactional
    public void inicializarFilaParaItemEnCentros(UUID itemCatalogoId) {
        List<UUID> centroIds = repositorioCentros.findAll().stream()
                .map(c -> c.getId())
                .toList();
        for (UUID centroId : centroIds) {
            if (repositorioInventario.findByCentroIdAndItemCatalogoId(centroId, itemCatalogoId).isEmpty()) {
                repositorioInventario.save(Inventario.builder()
                        .centroId(centroId)
                        .itemCatalogoId(itemCatalogoId)
                        .stockActual(0L)
                        .umbralMinimo(UMBRAL_MINIMO_DEFECTO)
                        .umbralOptimo(UMBRAL_OPTIMO_DEFECTO)
                        .umbralMaximo(UMBRAL_MAXIMO_DEFECTO)
                        .estadoCriticidad(EstadoCriticidad.AGOTADO)
                        .build());
            }
        }
    }

    @Transactional(readOnly = true)
    public List<InventarioItemResponse> listarPorCentro(UUID centroId) {
        verificarCentroExiste(centroId);
        List<Inventario> filas = repositorioInventario.findByCentroIdOrderByItemCatalogoIdAsc(centroId);
        return mapearFilasInventario(filas);
    }

    @Transactional(readOnly = true)
    public List<ResumenInventarioCategoriaResponse> resumenPorCategoria(UUID centroId) {
        verificarCentroExiste(centroId);
        return repositorioInventario.resumenPorCategoria(centroId).stream()
                .map(this::aResumenCategoria)
                .toList();
    }

    @Transactional
    @CacheEvict(value = {ServicioMapData.CACHE_MAP_DATA, ServicioKpis.CACHE_KPIS}, allEntries = true)
    public RespuestaMovimientoInventarioResponse registrarMovimiento(UUID centroId,
                                                                     SolicitudMovimientoInventarioRequest solicitud) {
        verificarCentroExiste(centroId);
        ItemCatalogo item = repositorioCatalogoItems.findByIdAndActivoTrue(solicitud.itemCatalogoId())
                .orElseThrow(() -> new ItemCatalogoNoEncontradoException(solicitud.itemCatalogoId()));
        Categoria categoria = repositorioCategorias.findById(item.getCategoriaId())
                .orElseThrow(() -> new ItemCatalogoNoEncontradoException(solicitud.itemCatalogoId()));

        Inventario inventario = repositorioInventario
                .findByCentroIdAndItemCatalogoId(centroId, solicitud.itemCatalogoId())
                .orElseThrow(() -> new InventarioNoEncontradoException(centroId, solicitud.itemCatalogoId()));

        long stockAnterior = inventario.getStockActual();
        EstadoCriticidad criticidadAnterior = inventario.getEstadoCriticidad();
        long stockNuevo = calcularStockNuevo(stockAnterior, solicitud.tipoMovimiento(), solicitud.cantidad());
        inventario.setStockActual(stockNuevo);
        inventario.setEstadoCriticidad(calcularCriticidad(inventario));

        Inventario actualizado = repositorioInventario.save(inventario);
        UUID usuarioId = contextoUsuario.usuarioIdActual();
        CategoriaInventario categoriaEnum = aCategoriaInventario(categoria.getCodigo());

        MovimientoInventario movimiento = MovimientoInventario.builder()
                .inventarioId(actualizado.getId())
                .centroId(centroId)
                .itemCatalogoId(item.getId())
                .categoria(categoriaEnum)
                .tipoMovimiento(solicitud.tipoMovimiento())
                .cantidad(solicitud.cantidad())
                .stockAnterior(stockAnterior)
                .stockPosterior(stockNuevo)
                .estadoCriticidadPosterior(actualizado.getEstadoCriticidad())
                .registradoPorUsuarioId(usuarioId)
                .build();
        MovimientoInventario movimientoPersistido = repositorioMovimientosInventario.save(movimiento);

        log.info("Movimiento inventario centro={} item={} {} cantidad={} stock={}->{}",
                centroId, item.getNombre(), solicitud.tipoMovimiento(),
                solicitud.cantidad(), stockAnterior, stockNuevo);

        publicarEventosTrasCommit(actualizado, movimientoPersistido, item, categoria,
                stockAnterior, criticidadAnterior, usuarioId);

        return new RespuestaMovimientoInventarioResponse(
                item.getId(),
                item.getNombre(),
                categoria.getCodigo(),
                solicitud.tipoMovimiento(),
                solicitud.cantidad(),
                stockAnterior,
                actualizado.getStockActual(),
                actualizado.getEstadoCriticidad(),
                actualizado.getActualizadoEn()
        );
    }

    @Transactional
    @CacheEvict(value = {ServicioMapData.CACHE_MAP_DATA, ServicioKpis.CACHE_KPIS}, allEntries = true)
    public InventarioItemResponse actualizarUmbrales(UUID centroId,
                                                     ActualizarUmbralesInventarioRequest solicitud) {
        verificarCentroExiste(centroId);
        validarUmbrales(solicitud.umbralMinimo(), solicitud.umbralOptimo(), solicitud.umbralMaximo());

        Inventario inventario = repositorioInventario
                .findByCentroIdAndItemCatalogoId(centroId, solicitud.itemCatalogoId())
                .orElseThrow(() -> new InventarioNoEncontradoException(centroId, solicitud.itemCatalogoId()));

        inventario.setUmbralMinimo(solicitud.umbralMinimo());
        inventario.setUmbralOptimo(solicitud.umbralOptimo());
        inventario.setUmbralMaximo(solicitud.umbralMaximo());
        inventario.setEstadoCriticidad(calcularCriticidad(inventario));

        Inventario guardado = repositorioInventario.save(inventario);
        return mapearFilasInventario(List.of(guardado)).getFirst();
    }

    public EstadoCriticidad calcularCriticidad(Inventario inventario) {
        long stock = inventario.getStockActual();
        if (stock == 0) {
            return EstadoCriticidad.AGOTADO;
        }
        if (stock <= inventario.getUmbralMinimo()) {
            return EstadoCriticidad.CRITICO;
        }
        if (stock <= inventario.getUmbralOptimo()) {
            return EstadoCriticidad.NORMAL;
        }
        if (stock <= inventario.getUmbralMaximo()) {
            return EstadoCriticidad.ABUNDANTE;
        }
        return EstadoCriticidad.SOBRESTOCK;
    }

    private void publicarEventosTrasCommit(Inventario inventario,
                                           MovimientoInventario movimiento,
                                           ItemCatalogo item,
                                           Categoria categoria,
                                           long stockAnterior,
                                           EstadoCriticidad criticidadAnterior,
                                           UUID usuarioId) {
        CategoriaInventario categoriaEnum = aCategoriaInventario(categoria.getCodigo());
        Runnable accion = () -> {
            OffsetDateTime ahora = OffsetDateTime.now();
            String correlacion = movimiento.getId().toString();

            publicadorEventos.publicar(MovimientoInventarioRegistradoEvento.builder()
                    .eventoId(UUID.randomUUID())
                    .ocurridoEn(ahora)
                    .correlacionId(correlacion)
                    .versionEvento(PublicadorEventos.versionEvento())
                    .fuente(PublicadorEventos.fuenteEvento())
                    .movimientoId(movimiento.getId())
                    .inventarioId(inventario.getId())
                    .centroId(inventario.getCentroId())
                    .itemCatalogoId(item.getId())
                    .categoria(categoriaEnum)
                    .tipoMovimiento(movimiento.getTipoMovimiento())
                    .cantidad(movimiento.getCantidad())
                    .stockAnterior(stockAnterior)
                    .stockPosterior(inventario.getStockActual())
                    .estadoCriticidadPosterior(inventario.getEstadoCriticidad())
                    .registradoPorUsuarioId(usuarioId)
                    .build());

            publicadorEventos.publicar(StockActualizadoEvento.builder()
                    .eventoId(UUID.randomUUID())
                    .ocurridoEn(ahora)
                    .correlacionId(correlacion)
                    .versionEvento(PublicadorEventos.versionEvento())
                    .fuente(PublicadorEventos.fuenteEvento())
                    .inventarioId(inventario.getId())
                    .centroId(inventario.getCentroId())
                    .itemCatalogoId(item.getId())
                    .categoria(categoriaEnum)
                    .stockAnterior(stockAnterior)
                    .stockActual(inventario.getStockActual())
                    .estadoCriticidad(inventario.getEstadoCriticidad())
                    .build());

            if (esCritico(inventario.getEstadoCriticidad())
                    && !esCritico(criticidadAnterior)) {
                publicadorEventos.publicar(StockCriticoEvento.builder()
                        .eventoId(UUID.randomUUID())
                        .ocurridoEn(ahora)
                        .correlacionId(correlacion)
                        .versionEvento(PublicadorEventos.versionEvento())
                        .fuente(PublicadorEventos.fuenteEvento())
                        .inventarioId(inventario.getId())
                        .centroId(inventario.getCentroId())
                        .itemCatalogoId(item.getId())
                        .categoria(categoriaEnum)
                        .stockActual(inventario.getStockActual())
                        .estadoCriticidad(inventario.getEstadoCriticidad())
                        .build());
            }
        };

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    accion.run();
                }
            });
        } else {
            accion.run();
        }
    }

    private List<InventarioItemResponse> mapearFilasInventario(List<Inventario> filas) {
        if (filas.isEmpty()) {
            return List.of();
        }
        List<UUID> itemIds = filas.stream().map(Inventario::getItemCatalogoId).distinct().toList();
        Map<UUID, ItemCatalogo> items = repositorioCatalogoItems.findByIdIn(itemIds).stream()
                .collect(Collectors.toMap(ItemCatalogo::getId, Function.identity()));
        Map<UUID, Categoria> categorias = repositorioCategorias.findAll().stream()
                .collect(Collectors.toMap(Categoria::getId, Function.identity()));

        return filas.stream()
                .map(fila -> {
                    ItemCatalogo item = items.get(fila.getItemCatalogoId());
                    Categoria cat = item != null ? categorias.get(item.getCategoriaId()) : null;
                    return new InventarioItemResponse(
                            fila.getId(),
                            fila.getCentroId(),
                            fila.getItemCatalogoId(),
                            item != null ? item.getNombre() : "Item desconocido",
                            cat != null ? cat.getCodigo() : "",
                            cat != null ? cat.getNombre() : "",
                            item != null ? item.getUnidadMedida() : "",
                            fila.getStockActual(),
                            fila.getUmbralMinimo(),
                            fila.getUmbralOptimo(),
                            fila.getUmbralMaximo(),
                            fila.getEstadoCriticidad(),
                            fila.getActualizadoEn()
                    );
                })
                .toList();
    }

    private ResumenInventarioCategoriaResponse aResumenCategoria(ProyeccionResumenInventarioCategoria proyeccion) {
        return new ResumenInventarioCategoriaResponse(
                proyeccion.getCodigoCategoria(),
                proyeccion.getNombreCategoria(),
                proyeccion.getStockTotal(),
                EstadoCriticidad.valueOf(proyeccion.getEstadoCriticidadAgregado())
        );
    }

    static CategoriaInventario aCategoriaInventario(String codigo) {
        return CategoriaInventario.valueOf(codigo);
    }

    private static boolean esCritico(EstadoCriticidad estado) {
        return estado == EstadoCriticidad.CRITICO || estado == EstadoCriticidad.AGOTADO;
    }

    private long calcularStockNuevo(long stockActual, TipoMovimiento tipo, long cantidad) {
        if (tipo == TipoMovimiento.INGRESO) {
            return stockActual + cantidad;
        }
        if (stockActual < cantidad) {
            throw new StockInsuficienteException(stockActual, cantidad);
        }
        return stockActual - cantidad;
    }

    private void validarUmbrales(long minimo, long optimo, long maximo) {
        if (minimo > optimo || optimo > maximo) {
            throw new UmbralesInvalidosException(
                    "Los umbrales deben cumplir: minimo <= optimo <= maximo");
        }
    }

    private void verificarCentroExiste(UUID centroId) {
        if (!repositorioCentros.existsById(centroId)) {
            throw new CentroNoEncontradoException(centroId);
        }
    }
}
