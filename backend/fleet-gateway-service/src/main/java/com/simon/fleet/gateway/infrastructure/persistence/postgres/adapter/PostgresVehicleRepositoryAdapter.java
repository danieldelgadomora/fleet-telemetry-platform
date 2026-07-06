package com.simon.fleet.gateway.infrastructure.persistence.postgres.adapter;

import com.simon.fleet.gateway.domain.model.MovementStatus;
import com.simon.fleet.gateway.domain.model.Vehicle;
import com.simon.fleet.gateway.domain.model.VehiclePlate;
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
    public Optional<Vehicle> findById(VehiclePlate id) {
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
    public void markCacheCleared(VehiclePlate id, Instant when) {
        jpaRepository.markCacheCleared(id.value(), when);
    }

    @Override
    @Transactional
    public void markDataPurged(VehiclePlate id, Instant when) {
        jpaRepository.markDataPurged(id.value(), when);
    }

    @Override
    @Transactional
    public boolean completeIfBothConfirmed(VehiclePlate id) {
        int rowsUpdated = jpaRepository.completeIfBothConfirmed(id.value());
        return rowsUpdated > 0;
    }

    @Override
    @Transactional
    public void registerOrReactivate(VehiclePlate id, Instant registeredAt) {
        jpaRepository.registerOrReactivate(id.value(), registeredAt);
    }

    @Override
    @Transactional
    public boolean updatePosition(VehiclePlate id, double lat, double lng, Instant when) {
        return jpaRepository.updatePosition(id.value(), lat, lng, when) > 0;
    }

    @Override
    @Transactional
    public boolean markInAlert(VehiclePlate id) {
        return jpaRepository.markInAlert(id.value()) > 0;
    }

    @Override
    public List<Vehicle> findAllActive() {
        return jpaRepository.findAllByStatus(VehicleStatus.ACTIVE.name()).stream()
                .map(this::toDomain)
                .toList();
    }

    private Vehicle toDomain(VehicleJpaEntity entity) {
        return Vehicle.builder()
                .id(new VehiclePlate(entity.getPlate()))
                .status(VehicleStatus.valueOf(entity.getStatus()))
                .registeredAt(entity.getRegisteredAt())
                .cacheClearedAt(entity.getCacheClearedAt())
                .dataPurgedAt(entity.getDataPurgedAt())
                .lastLat(entity.getLastLat())
                .lastLng(entity.getLastLng())
                .lastReportedAt(entity.getLastReportedAt())
                .movementStatus(entity.getMovementStatus() == null ? null : MovementStatus.valueOf(entity.getMovementStatus()))
                .build();
    }
}
