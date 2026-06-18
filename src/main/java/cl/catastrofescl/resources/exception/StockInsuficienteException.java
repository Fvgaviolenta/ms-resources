package cl.catastrofescl.resources.exception;

public class StockInsuficienteException extends RuntimeException {

    public StockInsuficienteException(long stockActual, long cantidadSolicitada) {
        super("Stock insuficiente: actual=" + stockActual + ", solicitado=" + cantidadSolicitada);
    }
}
