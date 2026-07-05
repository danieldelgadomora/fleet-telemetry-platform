package com.simon.fleet.alerting.infrastructure.cache.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simon.fleet.alerting.domain.model.VehiclePlate;
import com.simon.fleet.alerting.domain.model.VehicleTrackingState;
import com.simon.fleet.alerting.domain.port.out.VehicleTrackingStatePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Guarda el estado de tracking (última coordenada distinta + desde cuándo) en Redis bajo el
 * namespace {@code alerting:tracking:*}, separado del namespace de ingestion-service aunque
 * ambos comparten la misma instancia de Redis. Sin TTL: este estado debe sobrevivir mientras
 * el vehículo exista, no expirar solo.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisVehicleTrackingStateAdapter implements VehicleTrackingStatePort {

    private static final String KEY_PREFIX = "alerting:tracking:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<VehicleTrackingState> find(VehiclePlate plate) {
        String json = redisTemplate.opsForValue().get(key(plate));
        if (json == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, VehicleTrackingState.class));
        } catch (JsonProcessingException e) {
            log.warn("No se pudo deserializar el estado de tracking de {}", plate, e);
            return Optional.empty();
        }
    }

    @Override
    public void save(VehicleTrackingState state) {
        try {
            String json = objectMapper.writeValueAsString(state);
            redisTemplate.opsForValue().set(key(state.plate()), json);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo serializar el VehicleTrackingState", e);
        }
    }

    @Override
    public void clear(VehiclePlate plate) {
        redisTemplate.delete(key(plate));
    }

    private String key(VehiclePlate plate) {
        return KEY_PREFIX + plate.value();
    }
}
