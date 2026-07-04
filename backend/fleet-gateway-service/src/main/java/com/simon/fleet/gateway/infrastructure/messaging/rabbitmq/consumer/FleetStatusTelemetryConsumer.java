package com.simon.fleet.gateway.infrastructure.messaging.rabbitmq.consumer;

import com.simon.fleet.contracts.telemetry.TelemetryReceivedEvent;
import com.simon.fleet.gateway.domain.model.VehicleId;
import com.simon.fleet.gateway.domain.port.in.HandleTelemetryReceivedUseCase;
import com.simon.fleet.gateway.infrastructure.messaging.rabbitmq.config.RabbitMqConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Cola propia de fleet-gateway-service, bindeada al mismo exchange/routing key que ya
 * consumen ingestion-service y alerting-service: mantiene la vista de lectura del dashboard
 * (última posición, EN_MOVIMIENTO/DETENIDO) sin duplicar ni tocar el histórico de MongoDB.
 */
@Component
@RequiredArgsConstructor
public class FleetStatusTelemetryConsumer {

    private final HandleTelemetryReceivedUseCase handleTelemetryReceivedUseCase;

    @RabbitListener(queues = RabbitMqConfig.FLEET_STATUS_TELEMETRY_QUEUE)
    public void onTelemetryReceived(TelemetryReceivedEvent event) {
        handleTelemetryReceivedUseCase.onTelemetryReceived(
                new VehicleId(event.vehicleId()), event.lat(), event.lng(), event.recordedAt());
    }
}
