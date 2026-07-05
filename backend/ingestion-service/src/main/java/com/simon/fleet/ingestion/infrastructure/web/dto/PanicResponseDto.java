package com.simon.fleet.ingestion.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resultado de procesar una activación del botón de pánico")
public record PanicResponseDto(
        @Schema(example = "ABC123")
        @JsonProperty("plate") String plate,

        @Schema(example = "TRIGGERED")
        String status,

        @Schema(example = "Alerta de pánico registrada")
        String message
) {
}
