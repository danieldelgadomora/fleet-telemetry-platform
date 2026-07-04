package com.simon.fleet.ingestion.infrastructure.web;

import com.simon.fleet.ingestion.domain.model.InvalidTelemetryPayloadException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

/**
 * Convierte los errores de payload (reglas de negocio incumplidas, JSON mal formado, tipos
 * inválidos) en un 400 con un cuerpo entendible, en vez de dejar que Spring devuelva un 500
 * genérico. Es lo que permite manejar con gracia peticiones con formato erróneo, como las que
 * envía el simulador de caos.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidTelemetryPayloadException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidPayload(InvalidTelemetryPayloadException ex) {
        return badRequest(ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleMalformedJson(HttpMessageNotReadableException ex) {
        return badRequest("JSON malformado o con tipos inválidos");
    }

    private ResponseEntity<Map<String, Object>> badRequest(String message) {
        Map<String, Object> body = Map.of(
                "timestamp", Instant.now().toString(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", message
        );
        return ResponseEntity.badRequest().body(body);
    }
}
