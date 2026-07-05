package com.simon.fleet.alerting.infrastructure.messaging.rabbitmq.config;

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
 * Topología de RabbitMQ que le corresponde a alerting-service: consume de sus dos colas
 * propias (telemetría y borrado de vehículo) y publica en {@code fleet.alerts} (alertas
 * generadas) y {@code vehicle.lifecycle} (confirmación de purga, parte de la Saga).
 *
 * <p>Los exchanges {@code fleet.telemetry} y {@code vehicle.lifecycle} también los declara
 * ingestion-service con el mismo nombre: declarar un exchange topic ya existente con la misma
 * configuración es una operación idempotente en RabbitMQ, así que no importa el orden en que
 * arranquen los dos servicios.
 */
@Configuration
public class RabbitMqConfig {

    public static final String FLEET_TELEMETRY_EXCHANGE = "fleet.telemetry";
    public static final String FLEET_ALERTS_EXCHANGE = "fleet.alerts";
    public static final String FLEET_PANIC_EXCHANGE = "fleet.panic";
    public static final String VEHICLE_LIFECYCLE_EXCHANGE = "vehicle.lifecycle";

    public static final String TELEMETRY_RECEIVED_KEY = "telemetry.received";
    public static final String ALERT_RAISED_KEY = "alert.raised";
    public static final String PANIC_RAISED_KEY = "panic.raised";
    public static final String VEHICLE_DELETION_REQUESTED_KEY = "vehicle.deletion.requested";
    public static final String VEHICLE_DATA_PURGED_KEY = "vehicle.data.purged";

    public static final String EVALUATE_QUEUE = "alerting.telemetry.evaluate";
    public static final String EVALUATE_DLQ = "alerting.telemetry.evaluate.dlq";
    private static final String EVALUATE_DLX = "alerting.telemetry.evaluate.dlx";

    public static final String PANIC_QUEUE = "alerting.panic.evaluate";
    public static final String PANIC_DLQ = "alerting.panic.evaluate.dlq";
    private static final String PANIC_DLX = "alerting.panic.evaluate.dlx";

    public static final String VEHICLE_DELETION_QUEUE = "alerting.vehicle.deletion";

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public TopicExchange fleetTelemetryExchange() {
        return new TopicExchange(FLEET_TELEMETRY_EXCHANGE);
    }

    @Bean
    public TopicExchange fleetAlertsExchange() {
        return new TopicExchange(FLEET_ALERTS_EXCHANGE);
    }

    @Bean
    public TopicExchange vehicleLifecycleExchange() {
        return new TopicExchange(VEHICLE_LIFECYCLE_EXCHANGE);
    }

    /** También lo declara ingestion-service (publisher); ver nota de clase sobre idempotencia. */
    @Bean
    public TopicExchange fleetPanicExchange() {
        return new TopicExchange(FLEET_PANIC_EXCHANGE);
    }

    @Bean
    public FanoutExchange evaluateDeadLetterExchange() {
        return new FanoutExchange(EVALUATE_DLX);
    }

    @Bean
    public Queue evaluateDeadLetterQueue() {
        return QueueBuilder.durable(EVALUATE_DLQ).build();
    }

    @Bean
    public Binding evaluateDeadLetterBinding() {
        return BindingBuilder.bind(evaluateDeadLetterQueue()).to(evaluateDeadLetterExchange());
    }

    @Bean
    public Queue telemetryEvaluateQueue() {
        return QueueBuilder.durable(EVALUATE_QUEUE)
                .withArgument("x-dead-letter-exchange", EVALUATE_DLX)
                .build();
    }

    @Bean
    public Binding telemetryEvaluateBinding() {
        return BindingBuilder.bind(telemetryEvaluateQueue())
                .to(fleetTelemetryExchange())
                .with(TELEMETRY_RECEIVED_KEY);
    }

    @Bean
    public FanoutExchange panicDeadLetterExchange() {
        return new FanoutExchange(PANIC_DLX);
    }

    @Bean
    public Queue panicDeadLetterQueue() {
        return QueueBuilder.durable(PANIC_DLQ).build();
    }

    @Bean
    public Binding panicDeadLetterBinding() {
        return BindingBuilder.bind(panicDeadLetterQueue()).to(panicDeadLetterExchange());
    }

    @Bean
    public Queue panicEvaluateQueue() {
        return QueueBuilder.durable(PANIC_QUEUE)
                .withArgument("x-dead-letter-exchange", PANIC_DLX)
                .build();
    }

    @Bean
    public Binding panicEvaluateBinding() {
        return BindingBuilder.bind(panicEvaluateQueue())
                .to(fleetPanicExchange())
                .with(PANIC_RAISED_KEY);
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
