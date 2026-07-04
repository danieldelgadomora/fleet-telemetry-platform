package com.simon.fleet.gateway.infrastructure.web;

import com.simon.fleet.gateway.domain.model.InvalidVehicleStateException;
import com.simon.fleet.gateway.domain.model.VehicleAlreadyRegisteredException;
import com.simon.fleet.gateway.domain.model.VehicleNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(VehicleNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(VehicleNotFoundException ex) {
        return body(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(InvalidVehicleStateException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidState(InvalidVehicleStateException ex) {
        return body(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(VehicleAlreadyRegisteredException.class)
    public ResponseEntity<Map<String, Object>> handleAlreadyRegistered(VehicleAlreadyRegisteredException ex) {
        return body(HttpStatus.CONFLICT, ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> body(HttpStatus status, String message) {
        Map<String, Object> payload = Map.of(
                "timestamp", Instant.now().toString(),
                "status", status.value(),
                "error", message
        );
        return ResponseEntity.status(status).body(payload);
    }
}
