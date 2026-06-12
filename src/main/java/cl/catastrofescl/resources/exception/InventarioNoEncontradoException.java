package cl.catastrofescl.resources.exception;

import cl.catastrofescl.resources.entity.CategoriaInventario;

import java.util.UUID;

public class InventarioNoEncontradoException extends RuntimeException {

    private final UUID centroId;
    private final CategoriaInventario categoria;

    public InventarioNoEncontradoException(UUID centroId, CategoriaInventario categoria) {
        super("Inventario no encontrado para centro " + centroId + " categoria " + categoria);
        this.centroId = centroId;
        this.categoria = categoria;
    }

    public UUID getCentroId() {
        return centroId;
    }

    public CategoriaInventario getCategoria() {
        return categoria;
    }
}
