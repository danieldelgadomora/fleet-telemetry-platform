package com.simon.fleet.ingestion.infrastructure.messaging.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declara la topología de RabbitMQ que le corresponde a ingestion-service: publica en
 * {@code fleet.telemetry} (lo que reportó un vehículo) y en {@code vehicle.lifecycle} (que ya
 * limpió su parte de un vehículo borrado), y consume de sus dos colas propias.
 *
 * <p>{@code fleet.telemetry}/{@code telemetry.received} tiene dos colas independientes
 * bindeadas (esta y la de alerting-service): es pub-sub real, cada servicio procesa el mismo
 * evento a su manera y a su propio ritmo.
 */
@Configuration
public class RabbitMqConfig {

    public static final String FLEET_TELEMETRY_EXCHANGE = "fleet.telemetry";
    public static final String VEHICLE_LIFECYCLE_EXCHANGE = "vehicle.lifecycle";

    public static final String TELEMETRY_RECEIVED_KEY = "telemetry.received";
    public static final String VEHICLE_DELETION_REQUESTED_KEY = "vehicle.deletion.requested";
    public static final String VEHICLE_CACHE_CLEARED_KEY = "vehicle.cache.cleared";

    public static final String PERSIST_QUEUE = "ingestion.telemetry.persist";
    public static final String PERSIST_DLQ = "ingestion.telemetry.persist.dlq";
    private static final String PERSIST_DLX = "ingestion.telemetry.persist.dlx";

    public static final String VEHICLE_DELETION_QUEUE = "ingestion.vehicle.deletion";

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public TopicExchange fleetTelemetryExchange() {
        return new TopicExchange(FLEET_TELEMETRY_EXCHANGE);
    }

    @Bean
    public TopicExchange vehicleLifecycleExchange() {
        return new TopicExchange(VEHICLE_LIFECYCLE_EXCHANGE);
    }

    /** Dead-letter exchange: fanout, porque la única cola que le importa es la DLQ de abajo. */
    @Bean
    public FanoutExchange persistDeadLetterExchange() {
        return new FanoutExchange(PERSIST_DLX);
    }

    @Bean
    public Queue persistDeadLetterQueue() {
        return QueueBuilder.durable(PERSIST_DLQ).build();
    }

    @Bean
    public Binding persistDeadLetterBinding() {
        return BindingBuilder.bind(persistDeadLetterQueue()).to(persistDeadLetterExchange());
    }

    /**
     * Cuando el Circuit Breaker de MongoDB está abierto y el consumer descarta el mensaje
     * (ver {@code TelemetryPersistenceConsumer}), RabbitMQ lo reenvía automáticamente a
     * {@code PERSIST_DLX} gracias a este argumento, en vez de perderlo o reintentar en bucle.
     */
    @Bean
    public Queue telemetryPersistQueue() {
        return QueueBuilder.durable(PERSIST_QUEUE)
                .withArgument("x-dead-letter-exchange", PERSIST_DLX)
                .build();
    }

    @Bean
    public Binding telemetryPersistBinding() {
        return BindingBuilder.bind(telemetryPersistQueue())
                .to(fleetTelemetryExchange())
                .with(TELEMETRY_RECEIVED_KEY);
    }

    @Bean
    public Queue vehicleDeletionQueue() {
        return QueueBuilder.durable(VEHICLE_DELETION_QUEUE).build();
    }

    @Bean
    public Binding vehicleDeletionBinding() {
        return BindingBuilder.bind(vehicleDeletionQueue())
                .to(vehicleLifecycleExchange())
                .with(VEHICLE_DELETION_REQUESTED_KEY);
    }
}
