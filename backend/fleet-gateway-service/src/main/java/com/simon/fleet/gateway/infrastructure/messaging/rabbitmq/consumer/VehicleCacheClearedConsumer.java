package com.simon.fleet.gateway.infrastructure.messaging.rabbitmq.consumer;

import com.simon.fleet.contracts.lifecycle.VehicleCacheClearedEvent;
import com.simon.fleet.gateway.domain.port.in.HandleCacheClearedUseCase;
import com.simon.fleet.gateway.domain.model.VehiclePlate;
import com.simon.fleet.gateway.infrastructure.messaging.rabbitmq.config.RabbitMqConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** Confirmación publicada por ingestion-service cuando ya limpió Redis y MongoDB. */
@Component
@RequiredArgsConstructor
public class VehicleCacheClearedConsumer {

    private final HandleCacheClearedUseCase handleCacheClearedUseCase;

    @RabbitListener(queues = RabbitMqConfig.CACHE_CLEARED_QUEUE)
    public void onCacheCleared(VehicleCacheClearedEvent event) {
        handleCacheClearedUseCase.onCacheCleared(new VehiclePlate(event.plate()), event.clearedAt());
    }
}
