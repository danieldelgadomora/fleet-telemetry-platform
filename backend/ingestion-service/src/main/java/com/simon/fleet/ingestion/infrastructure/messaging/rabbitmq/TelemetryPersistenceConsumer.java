package com.simon.fleet.ingestion.infrastructure.messaging.rabbitmq;

import com.simon.fleet.contracts.telemetry.TelemetryReceivedEvent;
import com.simon.fleet.ingestion.domain.port.in.PersistTelemetryHistoryUseCase;
import com.simon.fleet.ingestion.domain.model.Coordinates;
import com.simon.fleet.ingestion.domain.model.TelemetryPoint;
import com.simon.fleet.ingestion.domain.model.VehicleId;
import com.simon.fleet.ingestion.infrastructure.persistence.mongo.MongoUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consume el evento que el propio ingestion-service publicó, y recién aquí escribe en
 * MongoDB. Desacoplar esto del endpoint HTTP es lo que permite que una caída de Mongo nunca
 * tumbe la ingesta: si falla, el mensaje termina en la dead-letter-queue configurada en
 * {@link RabbitMqConfig#PERSIST_QUEUE}, no se pierde ni bloquea la cola principal.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TelemetryPersistenceConsumer {

    private final PersistTelemetryHistoryUseCase persistTelemetryHistoryUseCase;

    @RabbitListener(queues = RabbitMqConfig.PERSIST_QUEUE)
    public void onTelemetryReceived(TelemetryReceivedEvent event) {
        TelemetryPoint point = new TelemetryPoint(
                new VehicleId(event.vehicleId()),
                new Coordinates(event.lat(), event.lng()),
                event.recordedAt()
        );

        try {
            persistTelemetryHistoryUseCase.persist(point);
        } catch (MongoUnavailableException e) {
            log.warn("Mongo no disponible (circuit breaker), enviando a DLQ vehicleId={}", event.vehicleId(), e);
            throw new AmqpRejectAndDontRequeueException("Persistencia en Mongo falló, ver DLQ", e);
        }
    }
}
