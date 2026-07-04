package com.simon.fleet.alerting.domain.port.out;

import com.simon.fleet.alerting.domain.model.SafeZone;

import java.util.List;

/** Puerto de salida (driven) hacia el catálogo de zonas seguras (PostgreSQL). */
public interface GeofenceRepositoryPort {

    /** Zonas seguras activas, para evaluar si una coordenada cae dentro de alguna. */
    List<SafeZone> findAllActive();
}
