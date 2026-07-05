package com.simon.fleet.ingestion.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Payload del botón de pánico enviado por la app móvil del conductor: snake_case, igual que
 * {@code TelemetryRequestDto}. Las coordenadas y el mensaje son opcionales.
 */
@Schema(description = "Activación del botón de pánico desde la app móvil")
public record PanicRequestDto(
        @Schema(description = "Placa del vehículo", example = "ABC123")
        @JsonProperty("plate") String plate,

        @Schema(description = "Latitud de la última posición conocida, si hay alguna", example = "4.6")
        Double lat,

        @Schema(description = "Longitud de la última posición conocida, si hay alguna", example = "-74.08")
        Double lng,

        @Schema(description = "Nota opcional del conductor", example = "Vehículo interceptado")
        String message,

        @Schema(description = "Momento en que se presionó el botón, ISO-8601", example = "2026-07-05T21:35:44.172Z")
        String timestamp
) {
}
