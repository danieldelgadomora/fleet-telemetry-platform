package com.simon.fleet.gateway.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record VehicleResponseDto(
        @JsonProperty("vehicle_id") String vehicleId,
        String status,
        @JsonProperty("registered_at") Instant registeredAt,
        @JsonProperty("cache_cleared_at") Instant cacheClearedAt,
        @JsonProperty("data_purged_at") Instant dataPurgedAt,
        @JsonProperty("last_lat") Double lastLat,
        @JsonProperty("last_lng") Double lastLng,
        @JsonProperty("last_reported_at") Instant lastReportedAt,
        @JsonProperty("movement_status") String movementStatus
) {
}
