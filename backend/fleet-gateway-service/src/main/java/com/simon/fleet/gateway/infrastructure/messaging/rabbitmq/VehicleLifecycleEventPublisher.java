package com.simon.fleet.gateway.infrastructure.messaging.rabbitmq;

import com.simon.fleet.contracts.lifecycle.VehicleDeletionRequestedEvent;
import com.simon.fleet.gateway.domain.model.VehicleId;
import com.simon.fleet.gateway.domain.port.out.VehicleLifecycleEventPublisherPort;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class VehicleLifecycleEventPublisher implements VehicleLifecycleEventPublisherPort {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishDeletionRequested(VehicleId vehicleId) {
        VehicleDeletionRequestedEvent event = new VehicleDeletionRequestedEvent(vehicleId.value(), Instant.now());
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.VEHICLE_LIFECYCLE_EXCHANGE, RabbitMqConfig.VEHICLE_DELETION_REQUESTED_KEY, event);
    }
}
