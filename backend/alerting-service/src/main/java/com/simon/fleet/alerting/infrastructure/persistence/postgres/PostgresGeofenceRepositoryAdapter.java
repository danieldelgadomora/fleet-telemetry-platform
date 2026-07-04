package com.simon.fleet.alerting.infrastructure.persistence.postgres;

import com.simon.fleet.alerting.domain.model.Coordinates;
import com.simon.fleet.alerting.domain.model.SafeZone;
import com.simon.fleet.alerting.domain.port.out.GeofenceRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PostgresGeofenceRepositoryAdapter implements GeofenceRepositoryPort {

    private final SafeZoneJpaRepository jpaRepository;

    @Override
    public List<SafeZone> findAllActive() {
        return jpaRepository.findByActiveTrue().stream().map(this::toDomain).toList();
    }

    private SafeZone toDomain(SafeZoneJpaEntity entity) {
        return new SafeZone(
                entity.getId(),
                entity.getName(),
                new Coordinates(entity.getCenterLat(), entity.getCenterLng()),
                entity.getRadiusMeters()
        );
    }
}
