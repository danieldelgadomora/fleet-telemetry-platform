package com.simon.fleet.gateway.infrastructure.web.websocket;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

record AlertMessage(
        @JsonProperty("alert_id") String alertId,
        @JsonProperty("vehicle_id") String vehicleId,
        @JsonProperty("rule_code") String ruleCode,
        String message,
        @JsonProperty("raised_at") Instant raisedAt
) {
}
