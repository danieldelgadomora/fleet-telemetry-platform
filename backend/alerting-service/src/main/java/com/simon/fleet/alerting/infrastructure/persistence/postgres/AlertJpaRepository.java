package com.simon.fleet.alerting.infrastructure.persistence.postgres;

import org.springframework.data.jpa.repository.JpaRepository;

interface AlertJpaRepository extends JpaRepository<AlertJpaEntity, String> {

    void deleteByVehicleId(String vehicleId);
}
