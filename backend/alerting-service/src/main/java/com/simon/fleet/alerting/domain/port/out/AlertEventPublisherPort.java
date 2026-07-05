package com.simon.fleet.alerting.domain.port.out;

import com.simon.fleet.alerting.domain.model.Alert;
import com.simon.fleet.alerting.domain.model.VehiclePlate;

/**
 * Puerto de salida (driven) hacia RabbitMQ. Publica hechos de negocio (una alerta generada, o
 * la confirmación de una purga) para que cualquier interesado los consuma sin acoplarse a
 * alerting-service.
 */
public interface AlertEventPublisherPort {

    void publishRaised(Alert alert);

    /** Confirma, dentro de la Saga de borrado, que ya se purgaron las alertas del vehículo. */
    void publishDataPurged(VehiclePlate plate);
}
