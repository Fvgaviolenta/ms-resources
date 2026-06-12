package cl.catastrofescl.resources.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ManejadorGlobalExcepciones {

    private static final String BASE_TYPE = "https://catastrofescl.cl/errors/";

    @ExceptionHandler(CentroNoEncontradoException.class)
    public ProblemDetail manejarCentroNoEncontrado(CentroNoEncontradoException ex, HttpServletRequest request) {
        ProblemDetail detalle = base(HttpStatus.NOT_FOUND, "Centro no encontrado",
                ex.getMessage(), "center-not-found", request);
        detalle.setProperty("errorCode", "CENTER_NOT_FOUND");
        detalle.setProperty("centroId", ex.getCentroId());
        return detalle;
    }

    @ExceptionHandler(OperadorYaAsignadoException.class)
    public ProblemDetail manejarOperadorYaAsignado(OperadorYaAsignadoException ex, HttpServletRequest request) {
        ProblemDetail detalle = base(HttpStatus.CONFLICT, "Operador ya asignado",
                ex.getMessage(), "operator-already-assigned", request);
        detalle.setProperty("errorCode", "OPERATOR_ALREADY_ASSIGNED");
        detalle.setProperty("centroId", ex.getCentroId());
        detalle.setProperty("usuarioId", ex.getUsuarioId());
        return detalle;
    }

    @ExceptionHandler(InventarioNoEncontradoException.class)
    public ProblemDetail manejarInventarioNoEncontrado(InventarioNoEncontradoException ex, HttpServletRequest request) {
        ProblemDetail detalle = base(HttpStatus.NOT_FOUND, "Inventario no encontrado",
                ex.getMessage(), "inventory-not-found", request);
        detalle.setProperty("errorCode", "INVENTORY_NOT_FOUND");
        detalle.setProperty("centroId", ex.getCentroId());
        detalle.setProperty("categoria", ex.getCategoria());
        return detalle;
    }

    @ExceptionHandler(StockInsuficienteException.class)
    public ProblemDetail manejarStockInsuficiente(StockInsuficienteException ex, HttpServletRequest request) {
        ProblemDetail detalle = base(HttpStatus.CONFLICT, "Stock insuficiente",
                ex.getMessage(), "insufficient-stock", request);
        detalle.setProperty("errorCode", "INSUFFICIENT_STOCK");
        return detalle;
    }

    @ExceptionHandler({GeometriaInvalidaException.class, UmbralesInvalidosException.class})
    public ProblemDetail manejarValidacionNegocio(RuntimeException ex, HttpServletRequest request) {
        ProblemDetail detalle = base(HttpStatus.BAD_REQUEST, "Datos invalidos",
                ex.getMessage(), "validation-error", request);
        detalle.setProperty("errorCode", "VALIDATION_ERROR");
        return detalle;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail manejarValidacion(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
                errores.put(err.getField(),
                        err.getDefaultMessage() != null ? err.getDefaultMessage() : "valor invalido"));
        ProblemDetail detalle = base(HttpStatus.BAD_REQUEST, "Datos de entrada invalidos",
                "Alguno de los campos enviados no supero la validacion", "validation-error", request);
        detalle.setProperty("errorCode", "VALIDATION_ERROR");
        detalle.setProperty("errores", errores);
        return detalle;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail manejarConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        ProblemDetail detalle = base(HttpStatus.BAD_REQUEST, "Datos de entrada invalidos",
                ex.getMessage(), "validation-error", request);
        detalle.setProperty("errorCode", "VALIDATION_ERROR");
        return detalle;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail manejarJsonInvalido(HttpMessageNotReadableException ex, HttpServletRequest request) {
        ProblemDetail detalle = base(HttpStatus.BAD_REQUEST, "JSON invalido",
                "No fue posible parsear el cuerpo de la peticion", "invalid-json", request);
        detalle.setProperty("errorCode", "INVALID_JSON");
        return detalle;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail manejarAccesoDenegado(AccessDeniedException ex, HttpServletRequest request) {
        ProblemDetail detalle = base(HttpStatus.FORBIDDEN, "Acceso denegado",
                "No posee los permisos necesarios para esta operacion", "access-denied", request);
        detalle.setProperty("errorCode", "ACCESS_DENIED");
        return detalle;
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail manejarNoAutenticado(AuthenticationException ex, HttpServletRequest request) {
        ProblemDetail detalle = base(HttpStatus.UNAUTHORIZED, "No autenticado",
                "Debe autenticarse para consumir este recurso", "unauthenticated", request);
        detalle.setProperty("errorCode", "UNAUTHENTICATED");
        return detalle;
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail manejarEstadoIlegal(IllegalStateException ex, HttpServletRequest request) {
        ProblemDetail detalle = base(HttpStatus.UNAUTHORIZED, "Sin contexto de usuario",
                ex.getMessage(), "missing-user-context", request);
        detalle.setProperty("errorCode", "MISSING_USER_CONTEXT");
        return detalle;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail manejarGenerico(Exception ex, HttpServletRequest request) {
        log.error("Error no controlado", ex);
        ProblemDetail detalle = base(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno",
                "Ocurrio un error inesperado. Intente nuevamente mas tarde.", "internal-error", request);
        detalle.setProperty("errorCode", "INTERNAL_ERROR");
        return detalle;
    }

    private ProblemDetail base(HttpStatus estado, String titulo, String detalleTexto,
                               String tipoSlug, HttpServletRequest request) {
        ProblemDetail detalle = ProblemDetail.forStatusAndDetail(estado, detalleTexto);
        detalle.setTitle(titulo);
        detalle.setType(URI.create(BASE_TYPE + tipoSlug));
        detalle.setInstance(URI.create(request.getRequestURI()));
        detalle.setProperty("timestamp", OffsetDateTime.now());
        return detalle;
    }
}
