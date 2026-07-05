package com.simon.fleet.alerting.infrastructure.messaging.rabbitmq.consumer;

import com.simon.fleet.alerting.domain.model.VehiclePlate;
import com.simon.fleet.alerting.domain.port.in.RaisePanicAlertUseCase;
import com.simon.fleet.alerting.infrastructure.messaging.rabbitmq.config.RabbitMqConfig;
import com.simon.fleet.contracts.panic.PanicButtonPressedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Cola independiente de la de telemetría: consume {@code PanicButtonPressedEvent} y genera la
 * alerta correspondiente sin pasar por ninguna {@code AlertRule}, ya que un botón de pánico no
 * necesita evaluación, siempre debe convertirse en alerta.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PanicAlertConsumer {

    private final RaisePanicAlertUseCase raisePanicAlertUseCase;

    @RabbitListener(queues = RabbitMqConfig.PANIC_QUEUE)
    public void onPanicButtonPressed(PanicButtonPressedEvent event) {
        try {
            raisePanicAlertUseCase.raise(
                    new VehiclePlate(event.plate()),
                    event.lat(),
                    event.lng(),
                    event.message(),
                    event.triggeredAt()
            );
        } catch (RuntimeException e) {
            log.error("Falló generando la alerta de pánico para plate={}, enviando a DLQ", event.plate(), e);
            throw new AmqpRejectAndDontRequeueException("Generación de alerta de pánico falló, ver DLQ", e);
        }
    }
}
