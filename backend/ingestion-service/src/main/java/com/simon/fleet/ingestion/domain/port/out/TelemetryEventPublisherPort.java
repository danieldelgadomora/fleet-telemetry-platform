package com.simon.fleet.ingestion.domain.port.out;

import com.simon.fleet.ingestion.domain.model.TelemetryPoint;
import com.simon.fleet.ingestion.domain.model.VehicleId;

/**
 * Puerto de salida (driven) hacia el broker de eventos (RabbitMQ). El dominio publica hechos
 * que ya ocurrieron; no le importa quién los consuma después.
 */
public interface TelemetryEventPublisherPort {

    /**
     * Publica que una lectura GPS fue aceptada (pasó la validación y no es duplicada). La
     * persistencia en MongoDB y la evaluación de alertas ocurren de forma asíncrona, cada una
     * consumiendo este mismo evento desde su propia cola.
     */
    void publishReceived(TelemetryPoint point);

    /**
     * Confirma, dentro de la Saga de borrado, que este servicio ya limpió el caché y el
     * histórico del vehículo.
     */
    void publishCacheCleared(VehicleId vehicleId);
}
