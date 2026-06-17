package cl.catastrofescl.resources.exception;

import java.util.UUID;

public class ItemCatalogoNoEncontradoException extends RuntimeException {

    private final UUID itemCatalogoId;

    public ItemCatalogoNoEncontradoException(UUID itemCatalogoId) {
        super("Item de catalogo no encontrado o inactivo: " + itemCatalogoId);
        this.itemCatalogoId = itemCatalogoId;
    }

    public UUID getItemCatalogoId() {
        return itemCatalogoId;
    }
}
