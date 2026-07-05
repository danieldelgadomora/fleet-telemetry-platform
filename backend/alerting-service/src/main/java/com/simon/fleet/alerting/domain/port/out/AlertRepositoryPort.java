package com.simon.fleet.alerting.domain.port.out;

import com.simon.fleet.alerting.domain.model.Alert;
import com.simon.fleet.alerting.domain.model.VehiclePlate;

/**
 * Puerto de salida (driven) de persistencia de alertas (PostgreSQL). Las alertas son datos
 * transaccionales con bajo volumen relativo a la telemetría cruda, por eso viven en la base
 * relacional y no en MongoDB.
 */
public interface AlertRepositoryPort {

    void save(Alert alert);

    /** Participante de la Saga de borrado: purga las alertas históricas del vehículo. */
    void purgeByVehicle(VehiclePlate plate);
}
