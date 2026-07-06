package com.simon.fleet.gateway.infrastructure.persistence.postgres.adapter;

import com.simon.fleet.gateway.domain.model.MovementStatus;
import com.simon.fleet.gateway.domain.model.Vehicle;
import com.simon.fleet.gateway.domain.model.VehiclePlate;
import com.simon.fleet.gateway.domain.model.VehicleStatus;
import com.simon.fleet.gateway.domain.port.out.VehicleRepositoryPort;
import com.simon.fleet.gateway.infrastructure.persistence.postgres.entity.VehicleJpaEntity;
import com.simon.fleet.gateway.infrastructure.persistence.postgres.repository.VehicleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** Adaptador de {@link VehicleRepositoryPort} sobre PostgreSQL, vía Spring Data JPA. */
@Repository
@RequiredArgsConstructor
public class PostgresVehicleRepositoryAdapter implements VehicleRepositoryPort {

    private final VehicleJpaRepository jpaRepository;

    /** Busca el vehículo por placa y lo traduce a su representación de dominio. */
    @Override
    public Optional<Vehicle> findById(VehiclePlate id) {
        return jpaRepository.findById(id.value()).map(this::toDomain);
    }

    /** Persiste el vehículo completo (registro inicial o transición de la Saga). */
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

    /** Registra la confirmación de ingestion-service de que ya limpió su caché/histórico del vehículo. */
    @Override
    @Transactional
    public void markCacheCleared(VehiclePlate id, Instant when) {
        jpaRepository.markCacheCleared(id.value(), when);
    }

    /** Registra la confirmación de alerting-service de que ya purgó su histórico de alertas del vehículo. */
    @Override
    @Transactional
    public void markDataPurged(VehiclePlate id, Instant when) {
        jpaRepository.markDataPurged(id.value(), when);
    }

    /** Cierra la Saga (transición atómica a {@code DELETED}) si ambas confirmaciones ya llegaron. */
    @Override
    @Transactional
    public boolean completeIfBothConfirmed(VehiclePlate id) {
        int rowsUpdated = jpaRepository.completeIfBothConfirmed(id.value());
        return rowsUpdated > 0;
    }

    /** Registra la placa si es nueva, o la reactiva limpia si estaba {@code DELETED}. */
    @Override
    @Transactional
    public void registerOrReactivate(VehiclePlate id, Instant registeredAt) {
        jpaRepository.registerOrReactivate(id.value(), registeredAt);
    }

    /** Actualiza la última posición conocida y deriva el {@code movement_status}. */
    @Override
    @Transactional
    public boolean updatePosition(VehiclePlate id, double lat, double lng, Instant when) {
        return jpaRepository.updatePosition(id.value(), lat, lng, when) > 0;
    }

    /** Marca el vehículo en {@code ALERTA}. */
    @Override
    @Transactional
    public boolean markInAlert(VehiclePlate id) {
        return jpaRepository.markInAlert(id.value()) > 0;
    }

    /** Devuelve todos los vehículos {@code ACTIVE}, traducidos a su representación de dominio. */
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
