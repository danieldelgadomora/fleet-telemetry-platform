package com.simon.fleet.ingestion.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resultado de procesar una lectura GPS")
public record TelemetryResponseDto(
        @Schema(example = "ABC123")
        @JsonProperty("plate") String plate,

        @Schema(description = "ACCEPTED o DUPLICATE_IGNORED", example = "ACCEPTED")
        String status,

        @Schema(example = "Telemetría aceptada, la persistencia histórica ocurre de forma asíncrona")
        String message
) {
}
