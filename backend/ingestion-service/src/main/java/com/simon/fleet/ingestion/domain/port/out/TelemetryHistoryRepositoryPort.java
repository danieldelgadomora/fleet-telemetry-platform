package com.simon.fleet.ingestion.domain.port.out;

import com.simon.fleet.ingestion.domain.model.TelemetryPoint;
import com.simon.fleet.ingestion.domain.model.VehiclePlate;

/**
 * Puerto de salida (driven) hacia el histórico persistente de telemetría (MongoDB). Solo lo
 * invoca el consumer de RabbitMQ que procesa {@code TelemetryReceivedEvent}, nunca el
 * controlador HTTP directamente: la escritura en Mongo está protegida con Circuit Breaker y no
 * debe bloquear la respuesta al cliente que envió la coordenada.
 */
public interface TelemetryHistoryRepositoryPort {

    /**
     * Persiste una lectura en el histórico. Puede lanzar una excepción de infraestructura si
     * Mongo no está disponible; quien la llama (el consumer) decide cómo reaccionar (reintento,
     * dead-letter, etc.).
     */
    void save(TelemetryPoint point);

    /**
     * Participante de la Saga de borrado: elimina del histórico todas las lecturas de un
     * vehículo. Debe ser idempotente.
     */
    void purgeByVehicle(VehiclePlate plate);
}
