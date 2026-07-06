package com.simon.fleet.alerting.infrastructure.persistence.postgres.adapter;

import com.simon.fleet.alerting.domain.model.Coordinates;
import com.simon.fleet.alerting.domain.model.SafeZone;
import com.simon.fleet.alerting.domain.port.out.GeofenceRepositoryPort;
import com.simon.fleet.alerting.infrastructure.persistence.postgres.entity.SafeZoneJpaEntity;
import com.simon.fleet.alerting.infrastructure.persistence.postgres.repository.SafeZoneJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Adaptador de {@link GeofenceRepositoryPort} sobre PostgreSQL, vía Spring Data JPA. */
@Repository
@RequiredArgsConstructor
public class PostgresGeofenceRepositoryAdapter implements GeofenceRepositoryPort {

    private final SafeZoneJpaRepository jpaRepository;

    /** Devuelve las zonas seguras activas ya traducidas a value objects de dominio. */
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
