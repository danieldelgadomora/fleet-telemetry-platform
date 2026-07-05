package com.simon.fleet.gateway.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Alta de un vehículo en el registro")
public record RegisterVehicleRequestDto(
        @Schema(description = "Placa del vehículo", example = "ABC123")
        @JsonProperty("plate") String plate
) {
}
