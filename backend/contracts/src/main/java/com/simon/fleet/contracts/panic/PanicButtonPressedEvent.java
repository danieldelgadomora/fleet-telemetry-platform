package com.simon.fleet.contracts.panic;

import java.time.Instant;

/**
 * Evento publicado por ingestion-service cuando el conductor presiona el botón de pánico desde
 * la app móvil. A diferencia de {@code TelemetryReceivedEvent}, no representa una lectura GPS
 * periódica sino un hecho puntual iniciado por el conductor; alerting-service lo consume desde
 * una cola independiente para generar una alerta {@code PANIC_BUTTON} sin pasar por las reglas
 * de evaluación de telemetría (atadas a coordenadas y estado de tracking).
 *
 * @param eventId     identificador único del evento.
 * @param plate       placa del vehículo que activó el botón de pánico.
 * @param lat         latitud de la última posición conocida en el dispositivo al momento de
 *                    presionar el botón, o {@code null} si la app aún no tiene ninguna.
 * @param lng         longitud en las mismas condiciones que {@code lat}.
 * @param message     nota opcional que el conductor puede agregar al activar el botón, o
 *                    {@code null} si no la incluyó.
 * @param triggeredAt momento en que el conductor presionó el botón.
 */
public record PanicButtonPressedEvent(
        String eventId,
        String plate,
        Double lat,
        Double lng,
        String message,
        Instant triggeredAt
) {
}
