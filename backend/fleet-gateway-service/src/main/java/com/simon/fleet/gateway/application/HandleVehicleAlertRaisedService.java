package com.simon.fleet.gateway.application;

import com.simon.fleet.gateway.domain.model.VehicleId;
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
    public void onAlertRaised(VehicleId vehicleId, Instant raisedAt) {
        boolean updated = repositoryPort.markInAlert(vehicleId);
        if (!updated) {
            repositoryPort.registerIfAbsent(vehicleId, raisedAt);
            repositoryPort.markInAlert(vehicleId);
        }
        repositoryPort.findById(vehicleId).ifPresent(fleetStatusBroadcastPort::broadcastStatus);
    }
}
