package cl.catastrofescl.resources.exception;

public class StockInsuficienteException extends RuntimeException {

    public StockInsuficienteException(int stockActual, int cantidadSolicitada) {
        super("Stock insuficiente: actual=" + stockActual + ", solicitado=" + cantidadSolicitada);
    }
}
