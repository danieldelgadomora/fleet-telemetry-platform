package com.simon.fleet.ingestion.infrastructure.cache.redis;

import com.simon.fleet.ingestion.domain.model.TelemetryPoint;
import com.simon.fleet.ingestion.domain.port.out.TelemetryDeduplicationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

/**
 * Anti-duplicados basado en {@code SETNX} (aquí, {@code opsForValue().setIfAbsent}): la
 * primera lectura de una coordenada dentro de una ventana de tiempo "gana" la clave; cualquier
 * lectura idéntica que llegue después, dentro de la misma ventana, encuentra la clave ya
 * ocupada y se reporta como duplicada. Es atómico a nivel de Redis, así que dos peticiones
 * casi simultáneas nunca pasan ambas como "no duplicada".
 */
@Repository
@RequiredArgsConstructor
public class RedisTelemetryDeduplicationAdapter implements TelemetryDeduplicationPort {

    private final StringRedisTemplate redisTemplate;

    @Value("${ingestion.dedupe.window-seconds:10}")
    private long windowSeconds;

    @Value("${ingestion.dedupe.ttl-seconds:15}")
    private long ttlSeconds;

    /** Marca la coordenada como vista dentro de su ventana de tiempo; true si ya se había visto. */
    @Override
    public boolean isDuplicate(TelemetryPoint point) {
        long bucket = point.recordedAt().getEpochSecond() / windowSeconds;
        String key = RedisKeys.dedupe(point.plate(), point.coordinates(), bucket);

        Boolean firstTimeSeen = redisTemplate.opsForValue()
                .setIfAbsent(key, "1", Duration.ofSeconds(ttlSeconds));

        return firstTimeSeen == null || !firstTimeSeen;
    }
}
