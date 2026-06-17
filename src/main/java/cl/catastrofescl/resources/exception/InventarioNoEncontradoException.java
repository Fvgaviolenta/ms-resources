package cl.catastrofescl.resources.exception;

import java.util.UUID;

public class InventarioNoEncontradoException extends RuntimeException {

    private final UUID centroId;
    private final UUID itemCatalogoId;

    public InventarioNoEncontradoException(UUID centroId, UUID itemCatalogoId) {
        super("Inventario no encontrado para centro " + centroId + " item " + itemCatalogoId);
        this.centroId = centroId;
        this.itemCatalogoId = itemCatalogoId;
    }

    public UUID getCentroId() {
        return centroId;
    }

    public UUID getItemCatalogoId() {
        return itemCatalogoId;
    }
}
