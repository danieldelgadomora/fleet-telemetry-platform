package com.simon.fleet.gateway.application;

import com.simon.fleet.gateway.domain.model.VehiclePlate;
import com.simon.fleet.gateway.domain.port.in.HandleDataPurgedUseCase;
import com.simon.fleet.gateway.domain.port.out.FleetStatusBroadcastPort;
import com.simon.fleet.gateway.domain.port.out.VehicleRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class HandleDataPurgedService implements HandleDataPurgedUseCase {

    private final VehicleRepositoryPort repositoryPort;
    private final FleetStatusBroadcastPort fleetStatusBroadcastPort;

    @Override
    public void onDataPurged(VehiclePlate plate, Instant purgedAt) {
        repositoryPort.markDataPurged(plate, purgedAt);
        // Ver HandleCacheClearedService: solo una de las dos confirmaciones gana la condición
        // SQL, así que el broadcast de DELETED ocurre exactamente una vez.
        if (repositoryPort.completeIfBothConfirmed(plate)) {
            repositoryPort.findById(plate).ifPresent(fleetStatusBroadcastPort::broadcastStatus);
        }
    }
}
