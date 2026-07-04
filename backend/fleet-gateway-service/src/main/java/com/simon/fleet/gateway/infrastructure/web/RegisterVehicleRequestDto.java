package com.simon.fleet.gateway.infrastructure.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Alta de un vehículo en el registro")
public record RegisterVehicleRequestDto(
        @Schema(description = "Identificador del vehículo", example = "v1")
        @JsonProperty("vehicle_id") String vehicleId
) {
}
