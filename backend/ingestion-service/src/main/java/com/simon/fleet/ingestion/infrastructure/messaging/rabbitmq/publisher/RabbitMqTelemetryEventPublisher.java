package com.simon.fleet.ingestion.infrastructure.messaging.rabbitmq.publisher;

import com.simon.fleet.contracts.lifecycle.VehicleCacheClearedEvent;
import com.simon.fleet.contracts.telemetry.TelemetryReceivedEvent;
import com.simon.fleet.ingestion.domain.model.TelemetryPoint;
import com.simon.fleet.ingestion.domain.model.VehiclePlate;
import com.simon.fleet.ingestion.domain.port.out.TelemetryEventPublisherPort;
import com.simon.fleet.ingestion.infrastructure.messaging.rabbitmq.config.RabbitMqConfig;
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
                point.plate().value(),
                point.coordinates().lat(),
                point.coordinates().lng(),
                point.recordedAt()
        );
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.FLEET_TELEMETRY_EXCHANGE, RabbitMqConfig.TELEMETRY_RECEIVED_KEY, event);
    }

    @Override
    public void publishCacheCleared(VehiclePlate plate) {
        VehicleCacheClearedEvent event = new VehicleCacheClearedEvent(plate.value(), Instant.now());
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.VEHICLE_LIFECYCLE_EXCHANGE, RabbitMqConfig.VEHICLE_CACHE_CLEARED_KEY, event);
    }
}
