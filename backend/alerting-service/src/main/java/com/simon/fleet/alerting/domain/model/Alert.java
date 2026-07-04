package com.simon.fleet.alerting.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Una alerta generada por alguna {@code AlertRule}. Se construye con el Builder Pattern
 * (vía Lombok) porque tiene varios campos, y un builder es más legible que un constructor con
 * muchos parámetros posicionales.
 */
@Getter
@Builder
public class Alert {

    private final String alertId;
    private final VehicleId vehicleId;
    private final String ruleCode;
    private final String message;
    private final Instant raisedAt;
}
