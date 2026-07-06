package com.simon.fleet.ingestion.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Un punto del histórico de recorrido de un vehículo")
public record TelemetryHistoryPointDto(
        @Schema(example = "4.6") double lat,
        @Schema(example = "-74.08") double lng,

        @JsonProperty("recorded_at")
        @Schema(example = "2026-07-03T21:35:44.172Z") Instant recordedAt
) {
}
