package com.simon.fleet.gateway.application;

import com.simon.fleet.gateway.domain.model.Vehicle;
import com.simon.fleet.gateway.domain.model.VehicleId;
import com.simon.fleet.gateway.domain.port.in.FindVehicleUseCase;
import com.simon.fleet.gateway.domain.port.out.VehicleRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FindVehicleService implements FindVehicleUseCase {

    private final VehicleRepositoryPort repositoryPort;

    @Override
    public Optional<Vehicle> findById(VehicleId vehicleId) {
        return repositoryPort.findById(vehicleId);
    }
}
