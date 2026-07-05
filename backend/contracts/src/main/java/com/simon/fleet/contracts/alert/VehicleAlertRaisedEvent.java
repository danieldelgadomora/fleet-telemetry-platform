package com.simon.fleet.contracts.alert;

import java.time.Instant;

/**
 * Evento publicado por alerting-service cuando alguna {@code AlertRule} detecta una anomalía
 * (por ejemplo, un vehículo detenido). Cualquier consumidor interesado en alertas en vivo
 * (por ejemplo, para reenviarlas a un dashboard por WebSocket) se suscribe a este evento.
 *
 * @param alertId  identificador único de la alerta generada.
 * @param plate    placa del vehículo sobre el que se generó la alerta.
 * @param ruleCode código de la regla que la disparó (ej. "STOPPED_VEHICLE"), pensado para que
 *                 el consumidor pueda diferenciar el tipo de alerta sin parsear el mensaje.
 * @param message  descripción legible de la alerta, lista para mostrar en UI.
 * @param raisedAt momento en que se detectó la anomalía.
 */
public record VehicleAlertRaisedEvent(
        String alertId,
        String plate,
        String ruleCode,
        String message,
        Instant raisedAt
) {
}
