package com.simon.fleet.alerting.domain.port.out;

import com.simon.fleet.alerting.domain.model.VehiclePlate;
import com.simon.fleet.alerting.domain.model.VehicleTrackingState;

import java.util.Optional;

/**
 * Puerto de salida (driven) hacia el almacén (Redis) del estado de tracking por vehículo que
 * necesita {@code StoppedVehicleRule}. Distinto del caché de ingestion-service: mismo Redis,
 * namespace de claves separado.
 */
public interface VehicleTrackingStatePort {

    /** Busca el último estado de tracking conocido del vehículo (última coordenada distinta y desde cuándo). */
    Optional<VehicleTrackingState> find(VehiclePlate plate);

    /** Persiste (o sobrescribe) el estado de tracking del vehículo. */
    void save(VehicleTrackingState state);

    /** Participante de la Saga de borrado: limpia el estado de tracking del vehículo. */
    void clear(VehiclePlate plate);
}
