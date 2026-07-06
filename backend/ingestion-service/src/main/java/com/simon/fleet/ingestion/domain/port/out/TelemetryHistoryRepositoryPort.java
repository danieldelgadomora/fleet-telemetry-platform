package com.simon.fleet.ingestion.domain.port.out;

import com.simon.fleet.ingestion.domain.model.TelemetryPoint;
import com.simon.fleet.ingestion.domain.model.VehiclePlate;

import java.util.List;

/**
 * Puerto de salida (driven) hacia el histórico persistente de telemetría (MongoDB). La
 * escritura ({@code save}) solo la invoca el consumer de RabbitMQ que procesa
 * {@code TelemetryReceivedEvent}, nunca el controlador HTTP directamente: está protegida con
 * Circuit Breaker y no debe bloquear la respuesta al cliente que envió la coordenada. La
 * lectura ({@code findRecent}) sí la invoca el controlador HTTP directamente, bajo demanda del
 * dashboard: no hay ninguna razón para pasarla por RabbitMQ.
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

    /**
     * Las {@code limit} lecturas más recientes de un vehículo, en orden descendente de
     * {@code recordedAt} (la más reciente primero). Lista vacía si la placa nunca reportó
     * telemetría — este puerto no valida la existencia del vehículo, esa identidad no le
     * pertenece a ingestion-service.
     */
    List<TelemetryPoint> findRecent(VehiclePlate plate, int limit);
}
