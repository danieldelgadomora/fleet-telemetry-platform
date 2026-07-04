package com.simon.fleet.gateway.infrastructure.web;

import com.simon.fleet.gateway.domain.model.Vehicle;

final class VehicleResponseMapper {

    private VehicleResponseMapper() {
    }

    static VehicleResponseDto toDto(Vehicle vehicle) {
        return new VehicleResponseDto(
                vehicle.getId().value(),
                vehicle.getStatus().name(),
                vehicle.getRegisteredAt(),
                vehicle.getCacheClearedAt(),
                vehicle.getDataPurgedAt(),
                vehicle.getLastLat(),
                vehicle.getLastLng(),
                vehicle.getLastReportedAt(),
                vehicle.getMovementStatus() == null ? null : vehicle.getMovementStatus().name()
        );
    }
}
