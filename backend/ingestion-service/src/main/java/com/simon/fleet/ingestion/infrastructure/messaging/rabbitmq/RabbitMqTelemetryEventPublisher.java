package com.simon.fleet.ingestion.infrastructure.messaging.rabbitmq;

import com.simon.fleet.contracts.lifecycle.VehicleCacheClearedEvent;
import com.simon.fleet.contracts.telemetry.TelemetryReceivedEvent;
import com.simon.fleet.ingestion.domain.model.TelemetryPoint;
import com.simon.fleet.ingestion.domain.model.VehicleId;
import com.simon.fleet.ingestion.domain.port.out.TelemetryEventPublisherPort;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RabbitMqTelemetryEventPublisher implements TelemetryEventPublisherPort {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishReceived(TelemetryPoint point) {
        TelemetryReceivedEvent event = new TelemetryReceivedEvent(
                UUID.randomUUID().toString(),
                point.vehicleId().value(),
                point.coordinates().lat(),
                point.coordinates().lng(),
                point.recordedAt()
        );
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.FLEET_TELEMETRY_EXCHANGE, RabbitMqConfig.TELEMETRY_RECEIVED_KEY, event);
    }

    @Override
    public void publishCacheCleared(VehicleId vehicleId) {
        VehicleCacheClearedEvent event = new VehicleCacheClearedEvent(vehicleId.value(), Instant.now());
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.VEHICLE_LIFECYCLE_EXCHANGE, RabbitMqConfig.VEHICLE_CACHE_CLEARED_KEY, event);
    }
}
