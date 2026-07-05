package com.simon.fleet.alerting.infrastructure.messaging.rabbitmq.consumer;

import com.simon.fleet.alerting.domain.port.in.EvaluateTelemetryUseCase;
import com.simon.fleet.alerting.domain.model.Coordinates;
import com.simon.fleet.alerting.domain.model.VehiclePlate;
import com.simon.fleet.alerting.domain.model.VehicleReading;
import com.simon.fleet.alerting.infrastructure.messaging.rabbitmq.config.RabbitMqConfig;
import com.simon.fleet.contracts.telemetry.TelemetryReceivedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Cola independiente de la de ingestion-service, aunque bindeada al mismo exchange/routing
 * key: es pub-sub real, cada servicio procesa el mismo {@code TelemetryReceivedEvent} a su
 * propio ritmo y sin conocer al otro.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TelemetryEvaluationConsumer {

    private final EvaluateTelemetryUseCase evaluateTelemetryUseCase;

    @RabbitListener(queues = RabbitMqConfig.EVALUATE_QUEUE)
    public void onTelemetryReceived(TelemetryReceivedEvent event) {
        VehicleReading reading = new VehicleReading(
                new VehiclePlate(event.plate()),
                new Coordinates(event.lat(), event.lng()),
                event.recordedAt()
        );

        try {
            evaluateTelemetryUseCase.evaluate(reading);
        } catch (RuntimeException e) {
            log.error("Falló evaluando alertas para plate={}, enviando a DLQ", event.plate(), e);
            throw new AmqpRejectAndDontRequeueException("Evaluación de alertas falló, ver DLQ", e);
        }
    }
}
