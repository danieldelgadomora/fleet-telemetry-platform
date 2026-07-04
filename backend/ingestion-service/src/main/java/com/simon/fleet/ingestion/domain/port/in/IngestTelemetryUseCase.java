package com.simon.fleet.ingestion.domain.port.in;

import com.simon.fleet.ingestion.domain.model.InvalidTelemetryPayloadException;
import com.simon.fleet.ingestion.domain.model.TelemetryPoint;

/**
 * Puerto de entrada (driving): caso de uso principal del servicio, invocado por el adaptador
 * web ({@code TelemetryController}). Es el camino rápido (fast path) de la ingesta: valida,
 * deduplica y cachea, pero nunca escribe en MongoDB directamente, para que la latencia o
 * disponibilidad de la base de datos histórica no afecten la respuesta HTTP.
 */
public interface IngestTelemetryUseCase {

    /**
     * @throws InvalidTelemetryPayloadException si el punto no pasa la cadena de validación.
     */
    TelemetryIngestionResult ingest(TelemetryPoint point);
}
