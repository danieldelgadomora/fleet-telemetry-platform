package com.simon.fleet.gateway.application;

import com.simon.fleet.gateway.domain.model.VehicleId;
import com.simon.fleet.gateway.domain.port.in.HandleTelemetryReceivedUseCase;
import com.simon.fleet.gateway.domain.port.out.FleetStatusBroadcastPort;
import com.simon.fleet.gateway.domain.port.out.VehicleRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class HandleTelemetryReceivedService implements HandleTelemetryReceivedUseCase {

    private final VehicleRepositoryPort repositoryPort;
    private final FleetStatusBroadcastPort fleetStatusBroadcastPort;

    @Override
    public void onTelemetryReceived(VehicleId vehicleId, double lat, double lng, Instant recordedAt) {
        boolean updated = repositoryPort.updatePosition(vehicleId, lat, lng, recordedAt);
        if (!updated) {
            // Vehículo nunca registrado (nunca pasó por POST /api/v1/vehicles): se da de alta
            // automáticamente, y luego sí se aplica la posición.
            repositoryPort.registerIfAbsent(vehicleId, recordedAt);
            repositoryPort.updatePosition(vehicleId, lat, lng, recordedAt);
        }
        // El movement_status resultante lo deriva Postgres en el propio UPDATE (ver
        // VehicleRepositoryPort#updatePosition); se relee para poder empujarlo al dashboard.
        repositoryPort.findById(vehicleId).ifPresent(fleetStatusBroadcastPort::broadcastStatus);
    }
}
