package com.simon.fleet.ingestion.infrastructure.persistence.mongo.adapter;

import com.simon.fleet.ingestion.domain.model.TelemetryPoint;
import com.simon.fleet.ingestion.domain.model.VehiclePlate;
import com.simon.fleet.ingestion.domain.port.out.TelemetryHistoryRepositoryPort;
import com.simon.fleet.ingestion.infrastructure.persistence.mongo.entity.TelemetryDocument;
import com.simon.fleet.ingestion.infrastructure.persistence.mongo.repository.TelemetryMongoRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adaptador hacia MongoDB. {@code save} está protegido con Circuit Breaker + Retry: si Mongo
 * falla varias veces seguidas, el breaker se abre y las siguientes llamadas fallan rápido (sin
 * ni siquiera intentar la conexión) hasta que Mongo se recupera, en vez de acumular hilos
 * bloqueados esperando un timeout.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MongoTelemetryHistoryRepository implements TelemetryHistoryRepositoryPort {

    private static final String CIRCUIT_BREAKER_NAME = "mongoTelemetry";

    private final TelemetryMongoRepository mongoRepository;

    @Override
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "saveFallback")
    @Retry(name = CIRCUIT_BREAKER_NAME)
    public void save(TelemetryPoint point) {
        mongoRepository.save(TelemetryDocument.fromDomain(point));
    }

    /**
     * Se invoca cuando el breaker está abierto o se agotaron los reintentos. No traga el
     * error: lo convierte en {@link MongoUnavailableException} para que
     * {@code TelemetryPersistenceConsumer} decida enviar el mensaje a la dead-letter-queue.
     */
    @SuppressWarnings("unused")
    private void saveFallback(TelemetryPoint point, Throwable throwable) {
        log.error("No se pudo persistir telemetría en Mongo para plate={}", point.plate(), throwable);
        throw new MongoUnavailableException("MongoDB no disponible", throwable);
    }

    @Override
    public void purgeByVehicle(VehiclePlate plate) {
        mongoRepository.deleteByPlate(plate.value());
    }

    /**
     * Lectura bajo demanda del dashboard: sin Circuit Breaker/Retry a propósito. Esa protección
     * existe en {@code save} porque bloquear la ingesta en caliente es crítico; una consulta de
     * historial no tiene ese requisito, y ningún otro endpoint de lectura del proyecto lo usa.
     */
    @Override
    public List<TelemetryPoint> findRecent(VehiclePlate plate, int limit) {
        return mongoRepository.findByPlateOrderByRecordedAtDesc(plate.value(), PageRequest.of(0, limit))
                .stream()
                .map(TelemetryDocument::toDomain)
                .toList();
    }
}
