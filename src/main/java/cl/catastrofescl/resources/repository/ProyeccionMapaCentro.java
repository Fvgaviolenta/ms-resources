package cl.catastrofescl.resources.repository;

import java.util.UUID;

public interface ProyeccionMapaCentro {

    UUID getId();

    String getNombre();

    String getRegion();

    String getEstado();

    double getLongitud();

    double getLatitud();

    String getCriticidadMaxima();
}
