package com.simon.fleet.gateway.domain.port.in;

import com.simon.fleet.gateway.domain.model.Alert;

import java.util.List;

/** Puerto de entrada (driving): lista el historial reciente de alertas para el dashboard. */
public interface ListAlertsUseCase {

    /** Devuelve las {@code limit} alertas más recientes de toda la flota. */
    List<Alert> listRecent(int limit);
}
