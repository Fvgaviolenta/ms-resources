package cl.catastrofescl.resources.service;

import cl.catastrofescl.resources.dto.request.ActualizarUmbralesInventarioRequest;
import cl.catastrofescl.resources.dto.request.SolicitudMovimientoInventarioRequest;
import cl.catastrofescl.resources.dto.response.InventarioCategoriaResponse;
import cl.catastrofescl.resources.dto.response.RespuestaMovimientoInventarioResponse;
import cl.catastrofescl.resources.entity.CategoriaInventario;
import cl.catastrofescl.resources.entity.EstadoCriticidad;
import cl.catastrofescl.resources.entity.Inventario;
import cl.catastrofescl.resources.entity.MovimientoInventario;
import cl.catastrofescl.resources.entity.TipoMovimiento;
import cl.catastrofescl.resources.event.MovimientoInventarioRegistradoEvento;
import cl.catastrofescl.resources.event.StockActualizadoEvento;
import cl.catastrofescl.resources.event.StockCriticoEvento;
import cl.catastrofescl.resources.exception.CentroNoEncontradoException;
import cl.catastrofescl.resources.exception.InventarioNoEncontradoException;
import cl.catastrofescl.resources.exception.StockInsuficienteException;
import cl.catastrofescl.resources.exception.UmbralesInvalidosException;
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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServicioInventario {

    private static final int UMBRAL_MINIMO_DEFECTO = 10;
    private static final int UMBRAL_OPTIMO_DEFECTO = 50;
    private static final int UMBRAL_MAXIMO_DEFECTO = 200;

    private final RepositorioInventario repositorioInventario;
    private final RepositorioMovimientosInventario repositorioMovimientosInventario;
    private final RepositorioCentros repositorioCentros;
    private final MapeadorCentros mapeadorCentros;
    private final PublicadorEventos publicadorEventos;
    private final ContextoUsuario contextoUsuario;

    @Transactional
    public void inicializarInventarioCentro(UUID centroId) {
        for (CategoriaInventario categoria : CategoriaInventario.values()) {
            Inventario fila = Inventario.builder()
                    .centroId(centroId)
                    .categoria(categoria)
                    .stockActual(0)
                    .umbralMinimo(UMBRAL_MINIMO_DEFECTO)
                    .umbralOptimo(UMBRAL_OPTIMO_DEFECTO)
                    .umbralMaximo(UMBRAL_MAXIMO_DEFECTO)
                    .estadoCriticidad(EstadoCriticidad.AGOTADO)
                    .build();
            repositorioInventario.save(fila);
        }
        log.debug("Inventario inicializado para centro {}", centroId);
    }

    @Transactional(readOnly = true)
    public List<InventarioCategoriaResponse> listarPorCentro(UUID centroId) {
        verificarCentroExiste(centroId);
        return repositorioInventario.findByCentroIdOrderByCategoriaAsc(centroId).stream()
                .map(mapeadorCentros::aInventarioResponse)
                .toList();
    }

    @Transactional
    @CacheEvict(value = {ServicioMapData.CACHE_MAP_DATA, ServicioKpis.CACHE_KPIS}, allEntries = true)
    public RespuestaMovimientoInventarioResponse registrarMovimiento(UUID centroId,
                                                                     SolicitudMovimientoInventarioRequest solicitud) {
        verificarCentroExiste(centroId);
        Inventario inventario = repositorioInventario
                .findByCentroIdAndCategoria(centroId, solicitud.categoria())
                .orElseThrow(() -> new InventarioNoEncontradoException(centroId, solicitud.categoria()));

        int stockAnterior = inventario.getStockActual();
        EstadoCriticidad criticidadAnterior = inventario.getEstadoCriticidad();
        int stockNuevo = calcularStockNuevo(stockAnterior, solicitud.tipoMovimiento(), solicitud.cantidad());
        inventario.setStockActual(stockNuevo);
        inventario.setEstadoCriticidad(calcularCriticidad(inventario));

        Inventario actualizado = repositorioInventario.save(inventario);
        UUID usuarioId = contextoUsuario.usuarioIdActual();

        MovimientoInventario movimiento = MovimientoInventario.builder()
                .inventarioId(actualizado.getId())
                .centroId(centroId)
                .categoria(solicitud.categoria())
                .tipoMovimiento(solicitud.tipoMovimiento())
                .cantidad(solicitud.cantidad())
                .stockAnterior(stockAnterior)
                .stockPosterior(stockNuevo)
                .estadoCriticidadPosterior(actualizado.getEstadoCriticidad())
                .registradoPorUsuarioId(usuarioId)
                .build();
        MovimientoInventario movimientoPersistido = repositorioMovimientosInventario.save(movimiento);

        log.info("Movimiento inventario centro={} categoria={} {} cantidad={} stock={}->{}",
                centroId, solicitud.categoria(), solicitud.tipoMovimiento(),
                solicitud.cantidad(), stockAnterior, stockNuevo);

        publicarEventosTrasCommit(actualizado, movimientoPersistido, stockAnterior, criticidadAnterior, usuarioId);

        return new RespuestaMovimientoInventarioResponse(
                actualizado.getCategoria(),
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
    public InventarioCategoriaResponse actualizarUmbrales(UUID centroId,
                                                          ActualizarUmbralesInventarioRequest solicitud) {
        verificarCentroExiste(centroId);
        validarUmbrales(solicitud.umbralMinimo(), solicitud.umbralOptimo(), solicitud.umbralMaximo());

        Inventario inventario = repositorioInventario
                .findByCentroIdAndCategoria(centroId, solicitud.categoria())
                .orElseThrow(() -> new InventarioNoEncontradoException(centroId, solicitud.categoria()));

        inventario.setUmbralMinimo(solicitud.umbralMinimo());
        inventario.setUmbralOptimo(solicitud.umbralOptimo());
        inventario.setUmbralMaximo(solicitud.umbralMaximo());
        inventario.setEstadoCriticidad(calcularCriticidad(inventario));

        return mapeadorCentros.aInventarioResponse(repositorioInventario.save(inventario));
    }

    public EstadoCriticidad calcularCriticidad(Inventario inventario) {
        int stock = inventario.getStockActual();
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
                                           int stockAnterior,
                                           EstadoCriticidad criticidadAnterior,
                                           UUID usuarioId) {
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
                    .categoria(inventario.getCategoria())
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
                    .categoria(inventario.getCategoria())
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
                        .categoria(inventario.getCategoria())
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

    private static boolean esCritico(EstadoCriticidad estado) {
        return estado == EstadoCriticidad.CRITICO || estado == EstadoCriticidad.AGOTADO;
    }

    private int calcularStockNuevo(int stockActual, TipoMovimiento tipo, int cantidad) {
        if (tipo == TipoMovimiento.INGRESO) {
            return stockActual + cantidad;
        }
        if (stockActual < cantidad) {
            throw new StockInsuficienteException(stockActual, cantidad);
        }
        return stockActual - cantidad;
    }

    private void validarUmbrales(int minimo, int optimo, int maximo) {
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

    public static List<CategoriaInventario> categoriasDisponibles() {
        return Arrays.asList(CategoriaInventario.values());
    }
}
