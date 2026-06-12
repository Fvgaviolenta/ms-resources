package cl.catastrofescl.resources.exception;

import java.util.UUID;

public class CentroNoEncontradoException extends RuntimeException {

    private final UUID centroId;

    public CentroNoEncontradoException(UUID centroId) {
        super("Centro de acopio no encontrado: " + centroId);
        this.centroId = centroId;
    }

    public UUID getCentroId() {
        return centroId;
    }
}
