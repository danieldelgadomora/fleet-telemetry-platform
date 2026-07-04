package com.simon.fleet.alerting.infrastructure.config;

import com.simon.fleet.alerting.domain.port.out.GeofenceRepositoryPort;
import com.simon.fleet.alerting.domain.rule.AlertRule;
import com.simon.fleet.alerting.domain.rule.SafeZoneAwareAlertRule;
import com.simon.fleet.alerting.domain.rule.StoppedVehicleRule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Aquí, y no en el dominio, vive la anotación de Spring: {@code StoppedVehicleRule} recibe su
 * umbral por constructor, sin saber que el valor vino de {@code application.yml}. Esto es lo
 * que mantiene el paquete {@code domain} completamente libre de dependencias de Spring.
 *
 * <p>El único bean {@code AlertRule} envuelve a {@code StoppedVehicleRule} con
 * {@code SafeZoneAwareAlertRule}: sigue siendo una única regla registrada, para que
 * {@code EvaluateTelemetryService} no reciba la versión cruda y la decorada a la vez.
 */
@Configuration
public class AlertRuleConfig {

    @Bean
    public AlertRule stoppedVehicleRule(
            @Value("${alerting.stopped-vehicle.threshold:1m}") Duration stoppedThreshold,
            GeofenceRepositoryPort geofenceRepositoryPort) {
        AlertRule base = new StoppedVehicleRule(stoppedThreshold);
        return new SafeZoneAwareAlertRule(base, geofenceRepositoryPort);
    }
}
