package com.simon.fleet.gateway.domain.port.in;

import com.simon.fleet.gateway.domain.model.Vehicle;

import java.util.List;

/** Puerto de entrada (driving): listado de vehículos activos para el dashboard. */
public interface ListActiveVehiclesUseCase {

    /** Devuelve todos los vehículos {@code ACTIVE}. */
    List<Vehicle> listActive();
}
