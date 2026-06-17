package cl.catastrofescl.resources.exception;

public class CategoriaDuplicadaException extends RuntimeException {

    public CategoriaDuplicadaException(String codigo) {
        super("Ya existe una categoria con codigo: " + codigo);
    }
}
