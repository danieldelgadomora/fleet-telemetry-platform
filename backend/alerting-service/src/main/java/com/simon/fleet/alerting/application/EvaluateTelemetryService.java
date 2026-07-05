package com.simon.fleet.alerting.application;

import com.simon.fleet.alerting.domain.model.Alert;
import com.simon.fleet.alerting.domain.model.VehicleReading;
import com.simon.fleet.alerting.domain.model.VehicleTrackingState;
import com.simon.fleet.alerting.domain.port.in.EvaluateTelemetryUseCase;
import com.simon.fleet.alerting.domain.port.out.AlertEventPublisherPort;
import com.simon.fleet.alerting.domain.port.out.AlertRepositoryPort;
import com.simon.fleet.alerting.domain.port.out.VehicleTrackingStatePort;
import com.simon.fleet.alerting.domain.rule.AlertEvaluationResult;
import com.simon.fleet.alerting.domain.rule.AlertRule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Recorre todas las {@code AlertRule} registradas (Strategy Pattern) para cada lectura que
 * llega. Todas las reglas comparten el mismo {@code VehicleTrackingState} por vehículo; si
 * conviven reglas con necesidades de estado muy distintas, cada una podría tener su propio
 * puerto de estado en vez de compartirlo.
 */
@Service
@RequiredArgsConstructor
public class EvaluateTelemetryService implements EvaluateTelemetryUseCase {

    private final List<AlertRule> alertRules;
    private final VehicleTrackingStatePort trackingStatePort;
    private final AlertRepositoryPort alertRepositoryPort;
    private final AlertEventPublisherPort eventPublisherPort;

    @Override
    public void evaluate(VehicleReading reading) {
        Optional<VehicleTrackingState> currentState = trackingStatePort.find(reading.plate());

        for (AlertRule rule : alertRules) {
            AlertEvaluationResult result = rule.evaluate(reading, currentState);
            trackingStatePort.save(result.updatedState());
            result.alert().ifPresent(this::raise);
        }
    }

    private void raise(Alert alert) {
        alertRepositoryPort.save(alert);
        eventPublisherPort.publishRaised(alert);
    }
}
