package com.simon.fleet.alerting.domain.model;

import java.time.Instant;

/**
 * Traducción, a un tipo propio del dominio de alerting-service, del
 * {@code TelemetryReceivedEvent} que llega por RabbitMQ. Se mapea en la capa de
 * infraestructura (el consumer) para que el dominio nunca dependa directamente del formato de
 * mensajería de otro servicio.
 */
public record VehicleReading(VehicleId vehicleId, Coordinates coordinates, Instant recordedAt) {
}
