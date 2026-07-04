package com.simon.fleet.ingestion.domain.port.in;

import com.simon.fleet.ingestion.domain.model.TelemetryPoint;

/**
 * Puerto de entrada (driving): escribe una lectura ya aceptada en el histórico persistente
 * (MongoDB). Solo lo invoca el consumer de RabbitMQ que procesa
 * {@code TelemetryReceivedEvent}, nunca el flujo HTTP síncrono — ver {@link IngestTelemetryUseCase}.
 */
public interface PersistTelemetryHistoryUseCase {

    /**
     * Puede propagar una excepción de infraestructura si Mongo está caído; el Circuit Breaker
     * vive en el adaptador de persistencia, no aquí.
     */
    void persist(TelemetryPoint point);
}
