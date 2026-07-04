package com.simon.fleet.alerting.infrastructure.persistence.postgres;

import com.simon.fleet.alerting.domain.model.Alert;
import com.simon.fleet.alerting.domain.model.VehicleId;
import com.simon.fleet.alerting.domain.port.out.AlertRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PostgresAlertRepositoryAdapter implements AlertRepositoryPort {

    private final AlertJpaRepository jpaRepository;

    @Override
    public void save(Alert alert) {
        AlertJpaEntity entity = new AlertJpaEntity(
                alert.getAlertId(),
                alert.getVehicleId().value(),
                alert.getRuleCode(),
                alert.getMessage(),
                alert.getRaisedAt()
        );
        jpaRepository.save(entity);
    }

    /**
     * {@code deleteByVehicleId} es una consulta de borrado derivada de Spring Data JPA: a
     * diferencia de {@code save}/{@code findById}, este tipo de método necesita una
     * transacción explícita para poder ejecutar el {@code DELETE}.
     */
    @Override
    @Transactional
    public void purgeByVehicle(VehicleId vehicleId) {
        jpaRepository.deleteByVehicleId(vehicleId.value());
    }
}
