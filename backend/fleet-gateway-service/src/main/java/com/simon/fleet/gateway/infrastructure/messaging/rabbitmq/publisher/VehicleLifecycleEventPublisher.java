package com.simon.fleet.gateway.infrastructure.messaging.rabbitmq.publisher;

import com.simon.fleet.contracts.lifecycle.VehicleDeletionRequestedEvent;
import com.simon.fleet.gateway.domain.model.VehiclePlate;
import com.simon.fleet.gateway.domain.port.out.VehicleLifecycleEventPublisherPort;
import com.simon.fleet.gateway.infrastructure.messaging.rabbitmq.config.RabbitMqConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class VehicleLifecycleEventPublisher implements VehicleLifecycleEventPublisherPort {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishDeletionRequested(VehiclePlate plate) {
        VehicleDeletionRequestedEvent event = new VehicleDeletionRequestedEvent(plate.value(), Instant.now());
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.VEHICLE_LIFECYCLE_EXCHANGE, RabbitMqConfig.VEHICLE_DELETION_REQUESTED_KEY, event);
    }
}
