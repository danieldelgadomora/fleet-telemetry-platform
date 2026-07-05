package com.simon.fleet.gateway.application;

import com.simon.fleet.gateway.domain.exception.VehicleAlreadyRegisteredException;
import com.simon.fleet.gateway.domain.model.Vehicle;
import com.simon.fleet.gateway.domain.model.VehiclePlate;
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
    public Vehicle register(VehiclePlate plate) {
        if (repositoryPort.findById(plate).isPresent()) {
            throw new VehicleAlreadyRegisteredException(plate);
        }
        Vehicle vehicle = Vehicle.register(plate, Instant.now(clock));
        repositoryPort.save(vehicle);
        return vehicle;
    }
}
