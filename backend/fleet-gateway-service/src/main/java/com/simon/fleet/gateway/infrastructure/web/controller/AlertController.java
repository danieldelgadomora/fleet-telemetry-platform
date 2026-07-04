package com.simon.fleet.gateway.infrastructure.web.controller;

import com.simon.fleet.gateway.domain.port.in.ListAlertsUseCase;
import com.simon.fleet.gateway.infrastructure.web.dto.AlertResponseDto;
import com.simon.fleet.gateway.infrastructure.web.mapper.AlertResponseMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Historial reciente de alertas de la flota, para que el dashboard no dependa únicamente del
 * push en vivo por WebSocket al abrir o recargar la página.
 */
@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
@Tag(name = "Alertas", description = "Historial reciente de alertas de la flota")
public class AlertController {

    private final ListAlertsUseCase listAlertsUseCase;

    @GetMapping
    @Operation(summary = "Lista las alertas más recientes de toda la flota")
    public ResponseEntity<List<AlertResponseDto>> listRecent(@RequestParam(defaultValue = "50") int limit) {
        List<AlertResponseDto> alerts = listAlertsUseCase.listRecent(limit).stream()
                .map(AlertResponseMapper::toDto)
                .toList();
        return ResponseEntity.ok(alerts);
    }
}
