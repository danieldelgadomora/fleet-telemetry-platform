package com.simon.fleet.alerting.domain.port.in;

import com.simon.fleet.alerting.domain.model.VehicleReading;

/**
 * Puerto de entrada (driving): punto de entrada del Servicio de Alertas/Ruteo. Por cada
 * lectura GPS que llega desde ingestion-service, evalúa todas las {@code AlertRule}
 * registradas.
 */
public interface EvaluateTelemetryUseCase {

    void evaluate(VehicleReading reading);
}
