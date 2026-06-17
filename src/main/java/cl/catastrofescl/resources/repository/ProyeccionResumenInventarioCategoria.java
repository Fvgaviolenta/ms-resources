package cl.catastrofescl.resources.repository;

import java.util.UUID;

public interface ProyeccionResumenInventarioCategoria {

    String getCodigoCategoria();

    String getNombreCategoria();

    long getStockTotal();

    String getEstadoCriticidadAgregado();
}
