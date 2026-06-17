package cl.catastrofescl.resources.exception;

public class CategoriaNoEncontradaException extends RuntimeException {

    private final String identificador;

    public CategoriaNoEncontradaException(String identificador) {
        super("Categoria no encontrada: " + identificador);
        this.identificador = identificador;
    }

    public String getIdentificador() {
        return identificador;
    }
}
