package cl.catastrofescl.resources.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface EventoDominio {

    UUID getEventoId();

    OffsetDateTime getOcurridoEn();

    String getCorrelacionId();

    String getRoutingKey();
}
