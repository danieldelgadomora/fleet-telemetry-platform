package com.simon.fleet.alerting.infrastructure.persistence.postgres.repository;

import com.simon.fleet.alerting.infrastructure.persistence.postgres.entity.AlertJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertJpaRepository extends JpaRepository<AlertJpaEntity, String> {

    void deleteByVehicleId(String vehicleId);
}
