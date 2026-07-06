package com.simon.fleet.gateway.application;

import com.simon.fleet.gateway.domain.exception.VehicleNotFoundException;
import com.simon.fleet.gateway.domain.model.Vehicle;
import com.simon.fleet.gateway.domain.model.VehiclePlate;
import com.simon.fleet.gateway.domain.port.in.DeleteVehicleUseCase;
import com.simon.fleet.gateway.domain.port.out.VehicleLifecycleEventPublisherPort;
import com.simon.fleet.gateway.domain.port.out.VehicleRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Arranca la Saga de eliminación de un vehículo: es el único punto de entrada, y solo avanza
 * si el vehículo está {@code ACTIVE} (la propia transición la valida {@code Vehicle}).
 */
@Service
@RequiredArgsConstructor
public class DeleteVehicleService implements DeleteVehicleUseCase {

    private final VehicleRepositoryPort repositoryPort;
    private final VehicleLifecycleEventPublisherPort eventPublisherPort;

    /**
     * Transiciona el vehículo a {@code PENDING_DELETION}, lo persiste y publica el evento que
     * arranca la limpieza coreografiada en ingestion-service y alerting-service.
     */
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
