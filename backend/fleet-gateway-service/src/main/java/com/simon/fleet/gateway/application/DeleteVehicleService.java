package com.simon.fleet.gateway.application;

import com.simon.fleet.gateway.domain.exception.VehicleNotFoundException;
import com.simon.fleet.gateway.domain.model.Vehicle;
import com.simon.fleet.gateway.domain.model.VehicleId;
import com.simon.fleet.gateway.domain.port.in.DeleteVehicleUseCase;
import com.simon.fleet.gateway.domain.port.out.VehicleLifecycleEventPublisherPort;
import com.simon.fleet.gateway.domain.port.out.VehicleRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteVehicleService implements DeleteVehicleUseCase {

    private final VehicleRepositoryPort repositoryPort;
    private final VehicleLifecycleEventPublisherPort eventPublisherPort;

    @Override
    public Vehicle requestDeletion(VehicleId vehicleId) {
        Vehicle vehicle = repositoryPort.findById(vehicleId)
                .orElseThrow(() -> new VehicleNotFoundException(vehicleId));

        vehicle.requestDeletion();
        repositoryPort.save(vehicle);
        eventPublisherPort.publishDeletionRequested(vehicleId);
        return vehicle;
    }
}
