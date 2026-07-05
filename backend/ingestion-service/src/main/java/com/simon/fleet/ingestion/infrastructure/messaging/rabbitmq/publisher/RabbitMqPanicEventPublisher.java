package com.simon.fleet.ingestion.infrastructure.messaging.rabbitmq.publisher;

import com.simon.fleet.contracts.panic.PanicButtonPressedEvent;
import com.simon.fleet.ingestion.domain.model.PanicButtonPress;
import com.simon.fleet.ingestion.domain.port.out.PanicEventPublisherPort;
import com.simon.fleet.ingestion.infrastructure.messaging.rabbitmq.config.RabbitMqConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RabbitMqPanicEventPublisher implements PanicEventPublisherPort {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishTriggered(PanicButtonPress press) {
        PanicButtonPressedEvent event = new PanicButtonPressedEvent(
                UUID.randomUUID().toString(),
                press.plate().value(),
                press.lat(),
                press.lng(),
                press.message(),
                press.triggeredAt()
        );
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.FLEET_PANIC_EXCHANGE, RabbitMqConfig.PANIC_RAISED_KEY, event);
    }
}
