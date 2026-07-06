package com.simon.fleet.alerting.application;

import com.simon.fleet.alerting.domain.model.Alert;
import com.simon.fleet.alerting.domain.model.VehiclePlate;
import com.simon.fleet.alerting.domain.port.in.RaisePanicAlertUseCase;
import com.simon.fleet.alerting.domain.port.out.AlertEventPublisherPort;
import com.simon.fleet.alerting.domain.port.out.AlertRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Construye y propaga la alerta {@code PANIC_BUTTON}, disparada por el conductor desde la app
 * móvil en vez de por una {@code AlertRule} evaluando telemetría. Comparte los mismos puertos de
 * persistencia y publicación que {@code EvaluateTelemetryService}
 * ({@code AlertRepositoryPort}, {@code AlertEventPublisherPort}): una alerta ya construida se
 * persiste y se publica exactamente igual, sin importar qué la haya disparado.
 */
@Service
@RequiredArgsConstructor
public class RaisePanicAlertService implements RaisePanicAlertUseCase {

    static final String RULE_CODE = "PANIC_BUTTON";

    private final AlertRepositoryPort alertRepositoryPort;
    private final AlertEventPublisherPort eventPublisherPort;

    /** Construye la alerta {@code PANIC_BUTTON} (con o sin posición/nota del conductor), la persiste y la publica. */
    @Override
    public void raise(VehiclePlate plate, Double lat, Double lng, String driverMessage, Instant triggeredAt) {
        Alert alert = Alert.builder()
                .alertId(UUID.randomUUID().toString())
                .plate(plate)
                .ruleCode(RULE_CODE)
                .message(buildMessage(plate, lat, lng, driverMessage))
                .raisedAt(triggeredAt)
                .build();

        alertRepositoryPort.save(alert);
        eventPublisherPort.publishRaised(alert);
    }

    private String buildMessage(VehiclePlate plate, Double lat, Double lng, String driverMessage) {
        String location = (lat != null && lng != null)
                ? " en (%s, %s)".formatted(lat, lng)
                : "";
        String note = (driverMessage == null || driverMessage.isBlank()) ? "" : ": " + driverMessage;
        return "Botón de pánico activado por %s%s%s".formatted(plate.value(), location, note);
    }
}
