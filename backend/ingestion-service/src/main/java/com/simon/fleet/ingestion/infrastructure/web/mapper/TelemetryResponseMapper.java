package com.simon.fleet.ingestion.infrastructure.web.mapper;

import com.simon.fleet.ingestion.domain.model.TelemetryIngestionResult;
import com.simon.fleet.ingestion.domain.model.TelemetryPoint;
import com.simon.fleet.ingestion.infrastructure.web.dto.TelemetryResponseDto;

/**
 * Traduce el resultado ya decidido por {@code IngestTelemetryUseCase} a la respuesta HTTP: el
 * mensaje legible es un detalle de presentación para el cliente, no una regla de negocio (esa ya
 * se aplicó antes, en la capa de aplicación) — separado del controller para que este no tenga
 * que construir el cuerpo de la respuesta él mismo.
 */
public final class TelemetryResponseMapper {

    private TelemetryResponseMapper() {
    }

    /** Arma el DTO de respuesta, con un mensaje distinto según si la lectura fue aceptada o era un duplicado. */
    public static TelemetryResponseDto toDto(TelemetryPoint point, TelemetryIngestionResult result) {
        String message = switch (result) {
            case ACCEPTED -> "Telemetría aceptada, la persistencia histórica ocurre de forma asíncrona";
            case DUPLICATE_IGNORED -> "Lectura duplicada dentro de la ventana de dedupe, ignorada";
        };
        return new TelemetryResponseDto(point.plate().value(), result.name(), message);
    }
}
