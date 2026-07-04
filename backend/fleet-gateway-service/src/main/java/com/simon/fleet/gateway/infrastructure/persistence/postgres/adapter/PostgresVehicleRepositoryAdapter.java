package com.simon.fleet.gateway.infrastructure.persistence.postgres.adapter;

import com.simon.fleet.gateway.domain.model.MovementStatus;
import com.simon.fleet.gateway.domain.model.Vehicle;
import com.simon.fleet.gateway.domain.model.VehicleId;
import com.simon.fleet.gateway.domain.model.VehicleStatus;
import com.simon.fleet.gateway.domain.port.out.VehicleRepositoryPort;
import com.simon.fleet.gateway.infrastructure.persistence.postgres.entity.VehicleJpaEntity;
import com.simon.fleet.gateway.infrastructure.persistence.postgres.repository.VehicleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PostgresVehicleRepositoryAdapter implements VehicleRepositoryPort {

    private final VehicleJpaRepository jpaRepository;

    @Override
    public Optional<Vehicle> findById(VehicleId id) {
        return jpaRepository.findById(id.value()).map(this::toDomain);
    }

    @Override
    public void save(Vehicle vehicle) {
        jpaRepository.save(new VehicleJpaEntity(
                vehicle.getId().value(),
                vehicle.getStatus().name(),
                vehicle.getRegisteredAt(),
                vehicle.getCacheClearedAt(),
                vehicle.getDataPurgedAt(),
                vehicle.getLastLat(),
                vehicle.getLastLng(),
                vehicle.getLastReportedAt(),
                vehicle.getMovementStatus() == null ? null : vehicle.getMovementStatus().name()
        ));
    }

    @Override
    @Transactional
    public void markCacheCleared(VehicleId id, Instant when) {
        jpaRepository.markCacheCleared(id.value(), when);
    }

    @Override
    @Transactional
    public void markDataPurged(VehicleId id, Instant when) {
        jpaRepository.markDataPurged(id.value(), when);
    }

    @Override
    @Transactional
    public boolean completeIfBothConfirmed(VehicleId id) {
        int rowsUpdated = jpaRepository.completeIfBothConfirmed(id.value());
        return rowsUpdated > 0;
    }

    @Override
    @Transactional
    public void registerIfAbsent(VehicleId id, Instant registeredAt) {
        jpaRepository.registerIfAbsent(id.value(), registeredAt);
    }

    @Override
    @Transactional
    public boolean updatePosition(VehicleId id, double lat, double lng, Instant when) {
        return jpaRepository.updatePosition(id.value(), lat, lng, when) > 0;
    }

    @Override
    @Transactional
    public boolean markInAlert(VehicleId id) {
        return jpaRepository.markInAlert(id.value()) > 0;
    }

    @Override
    public List<Vehicle> findAllActive() {
        return jpaRepository.findAllByStatus(VehicleStatus.ACTIVE.name()).stream()
                .map(this::toDomain)
                .toList();
    }

    private Vehicle toDomain(VehicleJpaEntity entity) {
        return Vehicle.rehydrate(
                new VehicleId(entity.getId()),
                VehicleStatus.valueOf(entity.getStatus()),
                entity.getRegisteredAt(),
                entity.getCacheClearedAt(),
                entity.getDataPurgedAt(),
                entity.getLastLat(),
                entity.getLastLng(),
                entity.getLastReportedAt(),
                entity.getMovementStatus() == null ? null : MovementStatus.valueOf(entity.getMovementStatus())
        );
    }
}
