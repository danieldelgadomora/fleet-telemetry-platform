package com.simon.fleet.gateway.infrastructure.web.mapper;

import com.simon.fleet.gateway.domain.model.Alert;
import com.simon.fleet.gateway.infrastructure.web.dto.AlertResponseDto;

public final class AlertResponseMapper {

    private AlertResponseMapper() {
    }

    public static AlertResponseDto toDto(Alert alert) {
        return new AlertResponseDto(
                alert.alertId(),
                alert.plate().value(),
                alert.ruleCode(),
                alert.message(),
                alert.raisedAt()
        );
    }
}
