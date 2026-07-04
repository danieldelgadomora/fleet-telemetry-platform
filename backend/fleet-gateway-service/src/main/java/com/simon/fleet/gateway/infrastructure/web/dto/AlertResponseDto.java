package com.simon.fleet.gateway.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record AlertResponseDto(
        @JsonProperty("alert_id") String alertId,
        @JsonProperty("vehicle_id") String vehicleId,
        @JsonProperty("rule_code") String ruleCode,
        String message,
        @JsonProperty("raised_at") Instant raisedAt
) {
}
