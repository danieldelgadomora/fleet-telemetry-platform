package com.simon.fleet.gateway.domain.port.in;

import com.simon.fleet.gateway.domain.model.Vehicle;
import com.simon.fleet.gateway.domain.model.VehiclePlate;

import java.util.Optional;

/**
 * Puerto de entrada (driving): consulta de solo lectura del estado de un vehículo, usada por
 * el endpoint de consulta y por cualquier otro cliente interesado en el avance de la Saga.
 */
public interface FindVehicleUseCase {

    Optional<Vehicle> findById(VehiclePlate plate);
}
