package com.simon.fleet.gateway.infrastructure.web.controller;

import com.simon.fleet.gateway.domain.port.in.DeleteVehicleUseCase;
import com.simon.fleet.gateway.domain.port.in.FindVehicleUseCase;
import com.simon.fleet.gateway.domain.port.in.ListActiveVehiclesUseCase;
import com.simon.fleet.gateway.domain.port.in.RegisterVehicleUseCase;
import com.simon.fleet.gateway.domain.exception.VehicleNotFoundException;
import com.simon.fleet.gateway.domain.model.Vehicle;
import com.simon.fleet.gateway.domain.model.VehiclePlate;
import com.simon.fleet.gateway.infrastructure.web.dto.RegisterVehicleRequestDto;
import com.simon.fleet.gateway.infrastructure.web.dto.VehicleResponseDto;
import com.simon.fleet.gateway.infrastructure.web.mapper.VehicleResponseMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Registro, listado y borrado de vehículos. El borrado es el orquestador del Saga de
 * eliminación: solo marca {@code PENDING_DELETION} y publica el evento, la limpieza real la
 * hacen ingestion-service y alerting-service de forma asíncrona (ver diagrama de secuencia en
 * el README). El listado es la vista de lectura para el dashboard: último estado conocido de
 * cada vehículo, mantenido al día suscribiéndose a los eventos de esos mismos servicios.
 */
@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
@Tag(name = "Vehículos", description = "Registro, listado y Saga de eliminación de vehículos")
public class VehicleController {

    private final RegisterVehicleUseCase registerVehicleUseCase;
    private final DeleteVehicleUseCase deleteVehicleUseCase;
    private final FindVehicleUseCase findVehicleUseCase;
    private final ListActiveVehiclesUseCase listActiveVehiclesUseCase;

    @PostMapping
    @Operation(summary = "Registra un vehículo")
    @ApiResponse(responseCode = "201", description = "Vehículo registrado")
    @ApiResponse(responseCode = "409", description = "El vehículo ya estaba registrado")
    public ResponseEntity<VehicleResponseDto> register(@RequestBody RegisterVehicleRequestDto request) {
        Vehicle vehicle = registerVehicleUseCase.register(new VehiclePlate(request.plate()));
        return ResponseEntity.status(HttpStatus.CREATED).body(VehicleResponseMapper.toDto(vehicle));
    }

    @GetMapping
    @Operation(summary = "Lista los vehículos activos y su último estado conocido (EN_MOVIMIENTO, DETENIDO, ALERTA)")
    public ResponseEntity<List<VehicleResponseDto>> listActive() {
        List<VehicleResponseDto> vehicles = listActiveVehiclesUseCase.listActive().stream()
                .map(VehicleResponseMapper::toDto)
                .toList();
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/{plate}")
    @Operation(summary = "Consulta el estado de un vehículo (útil para ver avanzar la Saga)")
    @ApiResponse(responseCode = "200", description = "Vehículo encontrado")
    @ApiResponse(responseCode = "404", description = "Vehículo no registrado")
    public ResponseEntity<VehicleResponseDto> findById(@PathVariable String plate) {
        Vehicle vehicle = findVehicleUseCase.findById(new VehiclePlate(plate))
                .orElseThrow(() -> new VehicleNotFoundException(new VehiclePlate(plate)));
        return ResponseEntity.ok(VehicleResponseMapper.toDto(vehicle));
    }

    @DeleteMapping("/{plate}")
    @Operation(
            summary = "Arranca la Saga de eliminación de un vehículo",
            description = """
                    Marca el vehículo como PENDING_DELETION y publica el evento inicial de la
                    Saga. La confirmación final (DELETED) llega asíncronamente cuando
                    ingestion-service y alerting-service terminan de limpiar su propia parte;
                    consulta GET /{plate} para ver el avance.
                    """
    )
    @ApiResponse(responseCode = "202", description = "Borrado solicitado, en progreso")
    @ApiResponse(responseCode = "404", description = "Vehículo no registrado")
    @ApiResponse(responseCode = "409", description = "El vehículo no está ACTIVE (ya se pidió su borrado o ya fue borrado)")
    public ResponseEntity<VehicleResponseDto> requestDeletion(@PathVariable String plate) {
        Vehicle vehicle = deleteVehicleUseCase.requestDeletion(new VehiclePlate(plate));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(VehicleResponseMapper.toDto(vehicle));
    }
}
