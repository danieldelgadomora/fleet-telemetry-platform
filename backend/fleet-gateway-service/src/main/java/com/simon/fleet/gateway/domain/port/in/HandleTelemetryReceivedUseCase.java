package com.simon.fleet.gateway.domain.port.in;

import com.simon.fleet.gateway.domain.model.VehicleId;

import java.time.Instant;

/**
 * Puerto de entrada (driving): mantiene la vista de lectura del dashboard al día con cada
 * lectura GPS que ingestion-service publica en {@code fleet.telemetry}. Si el vehículo nunca
 * pasó por {@code POST /api/v1/vehicles}, se autoregistra: el dashboard debe reflejar
 * cualquier vehículo que esté reportando telemetría, no solo los dados de alta explícitamente.
 */
public interface HandleTelemetryReceivedUseCase {

    /** Idempotente frente a reintentos: aplicar la misma lectura dos veces no cambia el resultado. */
    void onTelemetryReceived(VehicleId vehicleId, double lat, double lng, Instant recordedAt);
}
