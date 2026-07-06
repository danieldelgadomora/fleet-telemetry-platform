package com.simon.fleet.gateway.application;

import com.simon.fleet.gateway.domain.model.VehiclePlate;
import com.simon.fleet.gateway.domain.port.in.BroadcastAlertUseCase;
import com.simon.fleet.gateway.domain.port.out.AlertBroadcastPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Reenvía al dashboard, por WebSocket/STOMP, una alerta que ya fue levantada y persistida por
 * alerting-service — este servicio no decide si alertar, solo empuja el hecho en tiempo real a
 * los clientes conectados.
 */
@Service
@RequiredArgsConstructor
public class BroadcastAlertService implements BroadcastAlertUseCase {

    private final AlertBroadcastPort alertBroadcastPort;

    /** Empuja la alerta a los clientes suscritos a {@code /topic/alerts}. */
    @Override
    public void broadcastAlert(VehiclePlate plate, String alertId, String ruleCode, String message, Instant raisedAt) {
        alertBroadcastPort.broadcastAlert(plate, alertId, ruleCode, message, raisedAt);
    }
}
