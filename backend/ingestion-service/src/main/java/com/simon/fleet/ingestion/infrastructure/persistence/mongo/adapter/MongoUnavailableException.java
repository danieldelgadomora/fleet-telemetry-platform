package com.simon.fleet.ingestion.infrastructure.persistence.mongo.adapter;

/**
 * Se lanza desde el método de fallback del Circuit Breaker cuando MongoDB no está disponible
 * (o el breaker está abierto). Quien la atrapa (el consumer de RabbitMQ) decide qué hacer:
 * en este caso, mandar el mensaje a la dead-letter-queue en vez de perderlo.
 */
public class MongoUnavailableException extends RuntimeException {

    public MongoUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
