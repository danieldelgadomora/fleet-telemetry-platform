package com.simon.fleet.ingestion.infrastructure.web.mapper;

import com.simon.fleet.ingestion.domain.model.TelemetryPoint;
import com.simon.fleet.ingestion.infrastructure.web.dto.TelemetryHistoryPointDto;

import java.util.List;

/** Traduce el histórico de dominio ya ordenado cronológicamente al DTO de respuesta HTTP. */
public final class TelemetryHistoryMapper {

    private TelemetryHistoryMapper() {
    }

    public static List<TelemetryHistoryPointDto> toDto(List<TelemetryPoint> points) {
        return points.stream()
                .map(point -> new TelemetryHistoryPointDto(
                        point.coordinates().lat(),
                        point.coordinates().lng(),
                        point.recordedAt()
                ))
                .toList();
    }
}
