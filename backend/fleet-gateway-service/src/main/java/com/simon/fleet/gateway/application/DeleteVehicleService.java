package com.simon.fleet.gateway.application;

import com.simon.fleet.gateway.domain.exception.VehicleNotFoundException;
import com.simon.fleet.gateway.domain.model.Vehicle;
import com.simon.fleet.gateway.domain.model.VehiclePlate;
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
    public Vehicle requestDeletion(VehiclePlate plate) {
        Vehicle vehicle = repositoryPort.findById(plate)
                .orElseThrow(() -> new VehicleNotFoundException(plate));

        vehicle.requestDeletion();
        repositoryPort.save(vehicle);
        eventPublisherPort.publishDeletionRequested(plate);
        return vehicle;
    }
}
