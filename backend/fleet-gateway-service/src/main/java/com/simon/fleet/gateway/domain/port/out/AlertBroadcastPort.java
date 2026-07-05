package com.simon.fleet.gateway.domain.port.out;

import com.simon.fleet.gateway.domain.model.VehiclePlate;

import java.time.Instant;

/**
 * Puerto de salida hacia el canal de notificaciones de alertas del dashboard. A diferencia de
 * {@link FleetStatusBroadcastPort} (una foto del estado agregado de un vehículo), esto es la
 * notificación puntual de un evento de negocio concreto —una alerta ya levantada por
 * alerting-service—, pensada para un panel o lista de alertas en el dashboard, no para el mapa.
 */
public interface AlertBroadcastPort {

    /** Publica una alerta ya levantada, con los datos necesarios para mostrarla en la UI. */
    void broadcastAlert(VehiclePlate plate, String alertId, String ruleCode, String message, Instant raisedAt);
}
