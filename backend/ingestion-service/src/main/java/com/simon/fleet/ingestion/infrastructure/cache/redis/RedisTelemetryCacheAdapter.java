package com.simon.fleet.ingestion.infrastructure.cache.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simon.fleet.ingestion.domain.model.TelemetryPoint;
import com.simon.fleet.ingestion.domain.model.VehiclePlate;
import com.simon.fleet.ingestion.domain.port.out.TelemetryCachePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Collection;
import java.util.Optional;

/**
 * Adaptador Redis para "última posición conocida" por vehículo. Guarda el {@link TelemetryPoint}
 * serializado como JSON bajo una clave con TTL corto: si el vehículo deja de reportar, su
 * última posición simplemente expira sola.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class RedisTelemetryCacheAdapter implements TelemetryCachePort {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ingestion.cache.last-position-ttl-seconds:300}")
    private long lastPositionTtlSeconds;

    /** Lee y deserializa la última posición cacheada de la placa, vacío si no hay nada o falla la deserialización. */
    @Override
    public Optional<TelemetryPoint> findLastKnownPosition(VehiclePlate plate) {
        String json = redisTemplate.opsForValue().get(RedisKeys.lastPosition(plate));
        if (json == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, TelemetryPoint.class));
        } catch (JsonProcessingException e) {
            log.warn("No se pudo deserializar la última posición cacheada de {}", plate, e);
            return Optional.empty();
        }
    }

    /** Serializa y cachea la lectura como última posición conocida de su placa, con TTL corto. */
    @Override
    public void saveLastKnownPosition(TelemetryPoint point) {
        try {
            String json = objectMapper.writeValueAsString(point);
            redisTemplate.opsForValue().set(
                    RedisKeys.lastPosition(point.plate()), json, Duration.ofSeconds(lastPositionTtlSeconds));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo serializar el TelemetryPoint para cachearlo", e);
        }
    }

    /** Borra la última posición cacheada y las claves de dedupe de la placa (participante de la Saga de eliminación). */
    @Override
    public void clearVehicleCache(VehiclePlate plate) {
        redisTemplate.delete(RedisKeys.lastPosition(plate));

        // KEYS bloquea el servidor si hay millones de claves; para el volumen de este
        // prototipo es aceptable, pero en producción esto se reemplazaría por un SCAN
        // incremental (Cursor) para no afectar la latencia de otros clientes de Redis.
        Collection<String> dedupeKeys = redisTemplate.keys(RedisKeys.dedupePattern(plate));
        if (dedupeKeys != null && !dedupeKeys.isEmpty()) {
            redisTemplate.delete(dedupeKeys);
        }
    }
}
