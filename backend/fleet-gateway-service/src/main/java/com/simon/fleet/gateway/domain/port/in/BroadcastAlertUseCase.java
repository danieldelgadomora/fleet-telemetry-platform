package com.simon.fleet.gateway.domain.port.in;

import com.simon.fleet.gateway.domain.model.VehiclePlate;

import java.time.Instant;

/**
 * Puerto de entrada (driving) dedicado solo a reenviar al dashboard, en tiempo real, una alerta
 * ya levantada por alerting-service. No decide ninguna regla de negocio sobre el vehículo —esa
 * responsabilidad es de {@link HandleVehicleAlertRaisedUseCase}, que transiciona su estado a
 * ALERTA—, separación que le da a cada interfaz una única razón de cambio.
 */
public interface BroadcastAlertUseCase {

    /** Publica la alerta hacia los clientes suscritos al canal de notificaciones. */
    void broadcastAlert(VehiclePlate plate, String alertId, String ruleCode, String message, Instant raisedAt);
}
