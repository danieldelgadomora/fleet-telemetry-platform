package com.simon.fleet.gateway.domain.port.out;

import com.simon.fleet.gateway.domain.model.VehiclePlate;

/**
 * Puerto de salida (driven) hacia RabbitMQ: arranca la Saga de eliminación. ingestion-service y
 * alerting-service tienen sus propios consumers escuchando este evento para limpiar cada uno
 * su propio almacén de datos del vehículo.
 */
public interface VehicleLifecycleEventPublisherPort {

    void publishDeletionRequested(VehiclePlate plate);
}
