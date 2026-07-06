package com.simon.fleet.gateway.application;

import com.simon.fleet.gateway.domain.model.Vehicle;
import com.simon.fleet.gateway.domain.port.in.ListActiveVehiclesUseCase;
import com.simon.fleet.gateway.domain.port.out.VehicleRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/** Alimenta el listado principal del dashboard: los vehículos activos y su último estado conocido. */
@Service
@RequiredArgsConstructor
public class ListActiveVehiclesService implements ListActiveVehiclesUseCase {

    private final VehicleRepositoryPort repositoryPort;

    /** Devuelve todos los vehículos {@code ACTIVE}. */
    @Override
    public List<Vehicle> listActive() {
        return repositoryPort.findAllActive();
    }
}
