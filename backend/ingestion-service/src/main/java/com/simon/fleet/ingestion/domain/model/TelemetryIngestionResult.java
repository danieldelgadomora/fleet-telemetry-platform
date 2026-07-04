package com.simon.fleet.ingestion.domain.model;

/**
 * Resultado de intentar ingerir una lectura GPS. El controlador REST lo traduce a un código
 * HTTP: {@code ACCEPTED} y {@code DUPLICATE_IGNORED} son ambos un 202 (para el cliente, un
 * duplicado ignorado no es un error: su dato ya quedó representado en el sistema), la
 * diferencia solo importa para logs/métricas.
 */
public enum TelemetryIngestionResult {
    ACCEPTED,
    DUPLICATE_IGNORED
}
