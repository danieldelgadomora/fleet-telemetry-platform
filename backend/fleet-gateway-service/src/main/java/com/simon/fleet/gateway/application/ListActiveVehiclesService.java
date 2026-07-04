package com.simon.fleet.gateway.application;

import com.simon.fleet.gateway.domain.model.Vehicle;
import com.simon.fleet.gateway.domain.port.in.ListActiveVehiclesUseCase;
import com.simon.fleet.gateway.domain.port.out.VehicleRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListActiveVehiclesService implements ListActiveVehiclesUseCase {

    private final VehicleRepositoryPort repositoryPort;

    @Override
    public List<Vehicle> listActive() {
        return repositoryPort.findAllActive();
    }
}
