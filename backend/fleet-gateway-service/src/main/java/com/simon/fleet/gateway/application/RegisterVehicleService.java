package com.simon.fleet.gateway.application;

import com.simon.fleet.gateway.domain.model.Vehicle;
import com.simon.fleet.gateway.domain.model.VehicleId;
import com.simon.fleet.gateway.domain.port.in.RegisterVehicleUseCase;
import com.simon.fleet.gateway.domain.port.out.VehicleRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RegisterVehicleService implements RegisterVehicleUseCase {

    private final VehicleRepositoryPort repositoryPort;
    private final Clock clock;

    @Override
    public Vehicle register(VehicleId vehicleId) {
        return repositoryPort.findById(vehicleId)
                .orElseGet(() -> {
                    Vehicle vehicle = Vehicle.register(vehicleId, Instant.now(clock));
                    repositoryPort.save(vehicle);
                    return vehicle;
                });
    }
}
