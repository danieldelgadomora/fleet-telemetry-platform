package com.simon.fleet.alerting.infrastructure.persistence.postgres.adapter;

import com.simon.fleet.alerting.domain.model.Alert;
import com.simon.fleet.alerting.domain.model.VehiclePlate;
import com.simon.fleet.alerting.domain.port.out.AlertRepositoryPort;
import com.simon.fleet.alerting.infrastructure.persistence.postgres.entity.AlertJpaEntity;
import com.simon.fleet.alerting.infrastructure.persistence.postgres.repository.AlertJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** Adaptador de {@link AlertRepositoryPort} sobre PostgreSQL, vía Spring Data JPA. */
@Repository
@RequiredArgsConstructor
public class PostgresAlertRepositoryAdapter implements AlertRepositoryPort {

    private final AlertJpaRepository jpaRepository;

    /** Persiste la alerta como fila de la tabla {@code alerts}. */
    @Override
    public void save(Alert alert) {
        AlertJpaEntity entity = new AlertJpaEntity(
                alert.getAlertId(),
                alert.getPlate().value(),
                alert.getRuleCode(),
                alert.getMessage(),
                alert.getRaisedAt()
        );
        jpaRepository.save(entity);
    }

    /**
     * {@code deleteByPlate} es una consulta de borrado derivada de Spring Data JPA: a
     * diferencia de {@code save}/{@code findById}, este tipo de método necesita una
     * transacción explícita para poder ejecutar el {@code DELETE}.
     */
    @Override
    @Transactional
    public void purgeByVehicle(VehiclePlate plate) {
        jpaRepository.deleteByPlate(plate.value());
    }
}
