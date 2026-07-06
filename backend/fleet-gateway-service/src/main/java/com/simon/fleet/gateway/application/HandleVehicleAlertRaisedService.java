package com.simon.fleet.gateway.application;

import com.simon.fleet.gateway.domain.model.VehiclePlate;
import com.simon.fleet.gateway.domain.port.in.HandleVehicleAlertRaisedUseCase;
import com.simon.fleet.gateway.domain.port.out.FleetStatusBroadcastPort;
import com.simon.fleet.gateway.domain.port.out.VehicleRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class HandleVehicleAlertRaisedService implements HandleVehicleAlertRaisedUseCase {

    private final VehicleRepositoryPort repositoryPort;
    private final FleetStatusBroadcastPort fleetStatusBroadcastPort;

    @Override
    public void onAlertRaised(VehiclePlate plate, Instant raisedAt) {
        boolean updated = repositoryPort.markInAlert(plate);
        if (!updated) {
            // La placa nunca se registró o estaba DELETED: se da de alta/reactiva
            // automáticamente, y luego sí se marca la alerta.
            repositoryPort.registerOrReactivate(plate, raisedAt);
            repositoryPort.markInAlert(plate);
        }
        repositoryPort.findById(plate).ifPresent(fleetStatusBroadcastPort::broadcastStatus);
    }
}
