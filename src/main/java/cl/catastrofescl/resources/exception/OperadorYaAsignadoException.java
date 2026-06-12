package cl.catastrofescl.resources.exception;

import java.util.UUID;

public class OperadorYaAsignadoException extends RuntimeException {

    private final UUID centroId;
    private final UUID usuarioId;

    public OperadorYaAsignadoException(UUID centroId, UUID usuarioId) {
        super("El usuario " + usuarioId + " ya esta asignado al centro " + centroId);
        this.centroId = centroId;
        this.usuarioId = usuarioId;
    }

    public UUID getCentroId() {
        return centroId;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }
}
