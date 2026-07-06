package com.simon.fleet.gateway.application;

import com.simon.fleet.gateway.domain.model.VehiclePlate;
import com.simon.fleet.gateway.domain.port.in.HandleTelemetryReceivedUseCase;
import com.simon.fleet.gateway.domain.port.out.FleetStatusBroadcastPort;
import com.simon.fleet.gateway.domain.port.out.VehicleRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Mantiene al día la vista de lectura del dashboard consumiendo el evento de telemetría que ya
 * publica ingestion-service, sin tocar sus bases de datos ni su código.
 */
@Service
@RequiredArgsConstructor
public class HandleTelemetryReceivedService implements HandleTelemetryReceivedUseCase {

    private final VehicleRepositoryPort repositoryPort;
    private final FleetStatusBroadcastPort fleetStatusBroadcastPort;

    /**
     * Actualiza la última posición conocida; si la placa nunca se registró o estaba
     * {@code DELETED}, la da de alta/reactiva primero. Empuja el estado resultante al dashboard.
     */
    @Override
    public void onTelemetryReceived(VehiclePlate plate, double lat, double lng, Instant recordedAt) {
        boolean updated = repositoryPort.updatePosition(plate, lat, lng, recordedAt);
        if (!updated) {
            // La placa nunca se registró (nunca pasó por POST /api/v1/vehicles) o estaba
            // DELETED: en ambos casos se da de alta/reactiva automáticamente, y luego sí se
            // aplica la posición.
            repositoryPort.registerOrReactivate(plate, recordedAt);
            repositoryPort.updatePosition(plate, lat, lng, recordedAt);
        }
        // El movement_status resultante lo deriva Postgres en el propio UPDATE (ver
        // VehicleRepositoryPort#updatePosition); se relee para poder empujarlo al dashboard.
        repositoryPort.findById(plate).ifPresent(fleetStatusBroadcastPort::broadcastStatus);
    }
}
