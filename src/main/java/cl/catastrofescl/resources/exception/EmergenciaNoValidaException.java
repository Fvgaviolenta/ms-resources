package cl.catastrofescl.resources.exception;

import java.util.UUID;

/**
 * Se lanza cuando se intenta asociar un centro a una emergencia que no existe,
 * no esta activa o no pudo validarse contra ms-emergencies.
 */
public class EmergenciaNoValidaException extends RuntimeException {

    private final UUID emergenciaId;

    public EmergenciaNoValidaException(UUID emergenciaId, String mensaje) {
        super(mensaje);
        this.emergenciaId = emergenciaId;
    }

    public UUID getEmergenciaId() {
        return emergenciaId;
    }
}
