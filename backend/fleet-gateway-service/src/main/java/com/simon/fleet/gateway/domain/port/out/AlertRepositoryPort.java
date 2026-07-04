package com.simon.fleet.gateway.domain.port.out;

import com.simon.fleet.gateway.domain.model.Alert;

import java.util.List;

/**
 * Puerto de salida (driven) de solo lectura hacia el historial de alertas: fleet-gateway-service
 * nunca escribe una alerta, solo lista las que ya persistió alerting-service (dueño real de ese
 * dato) para servírselas al dashboard.
 */
public interface AlertRepositoryPort {

    /** Las {@code limit} alertas más recientes de toda la flota, la más nueva primero. */
    List<Alert> findRecent(int limit);
}
