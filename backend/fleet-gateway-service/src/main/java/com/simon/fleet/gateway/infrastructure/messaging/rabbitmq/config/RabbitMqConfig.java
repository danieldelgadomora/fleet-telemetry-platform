package com.simon.fleet.gateway.infrastructure.messaging.rabbitmq.config;

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
 * Topología de RabbitMQ que le corresponde a fleet-gateway-service. Tiene dos responsabilidades
 * distintas:
 *
 * <ul>
 *   <li>Orquestador de la Saga de eliminación: publica {@code vehicle.deletion.requested} y
 *       consume las dos confirmaciones ({@code vehicle.cache.cleared},
 *       {@code vehicle.data.purged}).</li>
 *   <li>Vista de lectura del dashboard: consume {@code fleet.telemetry} y {@code fleet.alerts},
 *       que ingestion-service y alerting-service ya publican, para mantener al día el último
 *       estado conocido de cada vehículo (ver {@code VehicleRepositoryPort#updatePosition}).</li>
 * </ul>
 *
 * <p>Los tres exchanges también los declaran los otros dos servicios con el mismo nombre;
 * redeclarar un exchange topic idéntico es una operación idempotente en RabbitMQ.
 */
@Configuration
public class RabbitMqConfig {

    public static final String VEHICLE_LIFECYCLE_EXCHANGE = "vehicle.lifecycle";
    public static final String FLEET_TELEMETRY_EXCHANGE = "fleet.telemetry";
    public static final String FLEET_ALERTS_EXCHANGE = "fleet.alerts";

    public static final String VEHICLE_DELETION_REQUESTED_KEY = "vehicle.deletion.requested";
    public static final String VEHICLE_CACHE_CLEARED_KEY = "vehicle.cache.cleared";
    public static final String VEHICLE_DATA_PURGED_KEY = "vehicle.data.purged";
    public static final String TELEMETRY_RECEIVED_KEY = "telemetry.received";
    public static final String ALERT_RAISED_KEY = "alert.raised";

    public static final String CACHE_CLEARED_QUEUE = "gateway.vehicle.cache-cleared";
    public static final String CACHE_CLEARED_DLQ = "gateway.vehicle.cache-cleared.dlq";
    private static final String CACHE_CLEARED_DLX = "gateway.vehicle.cache-cleared.dlx";

    public static final String DATA_PURGED_QUEUE = "gateway.vehicle.data-purged";
    public static final String DATA_PURGED_DLQ = "gateway.vehicle.data-purged.dlq";
    private static final String DATA_PURGED_DLX = "gateway.vehicle.data-purged.dlx";

    public static final String FLEET_STATUS_TELEMETRY_QUEUE = "gateway.fleet-status.telemetry";
    public static final String FLEET_STATUS_TELEMETRY_DLQ = "gateway.fleet-status.telemetry.dlq";
    private static final String FLEET_STATUS_TELEMETRY_DLX = "gateway.fleet-status.telemetry.dlx";

    public static final String FLEET_STATUS_ALERT_QUEUE = "gateway.fleet-status.alert";
    public static final String FLEET_STATUS_ALERT_DLQ = "gateway.fleet-status.alert.dlq";
    private static final String FLEET_STATUS_ALERT_DLX = "gateway.fleet-status.alert.dlx";

    private static final String DEAD_LETTER_EXCHANGE_ARG = "x-dead-letter-exchange";

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public TopicExchange vehicleLifecycleExchange() {
        return new TopicExchange(VEHICLE_LIFECYCLE_EXCHANGE);
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
    public FanoutExchange cacheClearedDeadLetterExchange() {
        return new FanoutExchange(CACHE_CLEARED_DLX);
    }

    @Bean
    public Queue cacheClearedDeadLetterQueue() {
        return QueueBuilder.durable(CACHE_CLEARED_DLQ).build();
    }

    @Bean
    public Binding cacheClearedDeadLetterBinding() {
        return BindingBuilder.bind(cacheClearedDeadLetterQueue()).to(cacheClearedDeadLetterExchange());
    }

    @Bean
    public Queue vehicleCacheClearedQueue() {
        return QueueBuilder.durable(CACHE_CLEARED_QUEUE)
                .withArgument(DEAD_LETTER_EXCHANGE_ARG, CACHE_CLEARED_DLX)
                .build();
    }

    @Bean
    public Binding vehicleCacheClearedBinding() {
        return BindingBuilder.bind(vehicleCacheClearedQueue())
                .to(vehicleLifecycleExchange())
                .with(VEHICLE_CACHE_CLEARED_KEY);
    }

    @Bean
    public FanoutExchange dataPurgedDeadLetterExchange() {
        return new FanoutExchange(DATA_PURGED_DLX);
    }

    @Bean
    public Queue dataPurgedDeadLetterQueue() {
        return QueueBuilder.durable(DATA_PURGED_DLQ).build();
    }

    @Bean
    public Binding dataPurgedDeadLetterBinding() {
        return BindingBuilder.bind(dataPurgedDeadLetterQueue()).to(dataPurgedDeadLetterExchange());
    }

    @Bean
    public Queue vehicleDataPurgedQueue() {
        return QueueBuilder.durable(DATA_PURGED_QUEUE)
                .withArgument(DEAD_LETTER_EXCHANGE_ARG, DATA_PURGED_DLX)
                .build();
    }

    @Bean
    public Binding vehicleDataPurgedBinding() {
        return BindingBuilder.bind(vehicleDataPurgedQueue())
                .to(vehicleLifecycleExchange())
                .with(VEHICLE_DATA_PURGED_KEY);
    }

    @Bean
    public FanoutExchange fleetStatusTelemetryDeadLetterExchange() {
        return new FanoutExchange(FLEET_STATUS_TELEMETRY_DLX);
    }

    @Bean
    public Queue fleetStatusTelemetryDeadLetterQueue() {
        return QueueBuilder.durable(FLEET_STATUS_TELEMETRY_DLQ).build();
    }

    @Bean
    public Binding fleetStatusTelemetryDeadLetterBinding() {
        return BindingBuilder.bind(fleetStatusTelemetryDeadLetterQueue()).to(fleetStatusTelemetryDeadLetterExchange());
    }

    /**
     * Cola propia de fleet-gateway-service bindeada al mismo exchange/routing key que ya
     * consumen ingestion-service (para persistir) y alerting-service (para evaluar alertas):
     * es pub-sub, cada servicio procesa el mismo evento a su propio ritmo.
     */
    @Bean
    public Queue fleetStatusTelemetryQueue() {
        return QueueBuilder.durable(FLEET_STATUS_TELEMETRY_QUEUE)
                .withArgument(DEAD_LETTER_EXCHANGE_ARG, FLEET_STATUS_TELEMETRY_DLX)
                .build();
    }

    @Bean
    public Binding fleetStatusTelemetryBinding() {
        return BindingBuilder.bind(fleetStatusTelemetryQueue())
                .to(fleetTelemetryExchange())
                .with(TELEMETRY_RECEIVED_KEY);
    }

    @Bean
    public FanoutExchange fleetStatusAlertDeadLetterExchange() {
        return new FanoutExchange(FLEET_STATUS_ALERT_DLX);
    }

    @Bean
    public Queue fleetStatusAlertDeadLetterQueue() {
        return QueueBuilder.durable(FLEET_STATUS_ALERT_DLQ).build();
    }

    @Bean
    public Binding fleetStatusAlertDeadLetterBinding() {
        return BindingBuilder.bind(fleetStatusAlertDeadLetterQueue()).to(fleetStatusAlertDeadLetterExchange());
    }

    @Bean
    public Queue fleetStatusAlertQueue() {
        return QueueBuilder.durable(FLEET_STATUS_ALERT_QUEUE)
                .withArgument(DEAD_LETTER_EXCHANGE_ARG, FLEET_STATUS_ALERT_DLX)
                .build();
    }

    @Bean
    public Binding fleetStatusAlertBinding() {
        return BindingBuilder.bind(fleetStatusAlertQueue())
                .to(fleetAlertsExchange())
                .with(ALERT_RAISED_KEY);
    }
}
