package com.simon.fleet.contracts.telemetry;

import java.time.Instant;

/**
 * Evento publicado por ingestion-service cada vez que una lectura GPS pasa la validación y
 * el chequeo de duplicados. Es el evento "fuente" que consumen tanto el propio
 * ingestion-service (para persistir el histórico en MongoDB) como alerting-service (para
 * evaluar reglas de alerta), cada uno desde su propia cola.
 *
 * @param eventId    identificador único del evento, útil para que los consumidores puedan
 *                   detectar reentregas (idempotencia) sin depender solo del contenido.
 * @param plate      placa del vehículo que reportó la coordenada.
 * @param lat        latitud en grados decimales.
 * @param lng        longitud en grados decimales.
 * @param recordedAt momento en que el vehículo generó la lectura (no el momento en que se
 *                   publicó el evento).
 */
public record TelemetryReceivedEvent(
        String eventId,
        String plate,
        double lat,
        double lng,
        Instant recordedAt
) {
}
