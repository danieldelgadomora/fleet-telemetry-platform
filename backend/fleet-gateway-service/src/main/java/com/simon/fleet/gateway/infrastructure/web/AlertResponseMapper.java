package com.simon.fleet.gateway.infrastructure.web;

import com.simon.fleet.gateway.domain.model.Alert;

final class AlertResponseMapper {

    private AlertResponseMapper() {
    }

    static AlertResponseDto toDto(Alert alert) {
        return new AlertResponseDto(
                alert.alertId(),
                alert.vehicleId().value(),
                alert.ruleCode(),
                alert.message(),
                alert.raisedAt()
        );
    }
}
