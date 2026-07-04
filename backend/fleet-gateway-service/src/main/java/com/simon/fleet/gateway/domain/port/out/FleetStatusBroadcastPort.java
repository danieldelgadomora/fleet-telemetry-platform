package com.simon.fleet.gateway.domain.port.out;

import com.simon.fleet.gateway.domain.model.Vehicle;

/**
 * Puerto de salida hacia el canal de estado agregado de la flota que consume el dashboard en
 * tiempo real: es la contraparte "push" de {@link VehicleRepositoryPort#findAllActive()}. Cada
 * vez que cambia la posición, el {@code movement_status} o el estado del ciclo de vida de un
 * vehículo, se publica una foto actualizada para que un cliente suscrito no necesite hacer
 * polling sobre el listado.
 */
public interface FleetStatusBroadcastPort {

    /** Publica el estado agregado actual del vehículo. */
    void broadcastStatus(Vehicle vehicle);
}
