package com.simon.fleet.gateway.application;

import com.simon.fleet.gateway.domain.model.Vehicle;
import com.simon.fleet.gateway.domain.model.VehiclePlate;
import com.simon.fleet.gateway.domain.port.in.FindVehicleUseCase;
import com.simon.fleet.gateway.domain.port.out.VehicleRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/** Consulta de lectura pura del estado actual de un vehículo, sin ningún efecto secundario. */
@Service
@RequiredArgsConstructor
public class FindVehicleService implements FindVehicleUseCase {

    private final VehicleRepositoryPort repositoryPort;

    /** Busca el vehículo por placa, vacío si nunca se registró. */
    @Override
    public Optional<Vehicle> findById(VehiclePlate plate) {
        return repositoryPort.findById(plate);
    }
}
