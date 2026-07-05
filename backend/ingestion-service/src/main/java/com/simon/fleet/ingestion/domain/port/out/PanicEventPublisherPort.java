package com.simon.fleet.ingestion.domain.port.out;

import com.simon.fleet.ingestion.domain.model.PanicButtonPress;

/**
 * Puerto de salida (driven) hacia RabbitMQ para el botón de pánico. Se mantiene separado de
 * {@code TelemetryEventPublisherPort} porque publica en un exchange distinto ({@code fleet.panic})
 * y no comparte forma de evento con la telemetría periódica.
 */
public interface PanicEventPublisherPort {

    void publishTriggered(PanicButtonPress press);
}
