package com.simon.fleet.ingestion.infrastructure.web.controller;

import com.simon.fleet.ingestion.domain.model.PanicButtonPress;
import com.simon.fleet.ingestion.domain.port.in.TriggerPanicButtonUseCase;
import com.simon.fleet.ingestion.infrastructure.web.dto.PanicRequestDto;
import com.simon.fleet.ingestion.infrastructure.web.dto.PanicResponseDto;
import com.simon.fleet.ingestion.infrastructure.web.mapper.PanicRequestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Punto de entrada del botón de pánico de la app móvil del conductor. Es una tubería paralela a
 * la de telemetría: publica un evento propio ({@code fleet.panic}) que alerting-service consume
 * para generar una alerta {@code PANIC_BUTTON}, sin pasar por la evaluación de {@code AlertRule}
 * de telemetría (atada a coordenadas y estado de tracking, no a un hecho puntual como este).
 */
@RestController
@RequestMapping("/api/v1/panic")
@RequiredArgsConstructor
@Tag(name = "Pánico", description = "Activación del botón de pánico desde la app móvil")
public class PanicController {

    private final TriggerPanicButtonUseCase triggerPanicButtonUseCase;

    @PostMapping
    @Operation(
            summary = "Registra la activación del botón de pánico de un conductor",
            description = """
                    Publica el evento correspondiente para que alerting-service genere una alerta
                    PANIC_BUTTON de forma asíncrona, visible en el dashboard y en el historial de
                    alertas igual que cualquier otra alerta.
                    """
    )
    @ApiResponse(responseCode = "202", description = "Activación registrada")
    @ApiResponse(responseCode = "400", description = "Payload inválido: placa faltante o timestamp no parseable")
    public ResponseEntity<PanicResponseDto> trigger(@RequestBody PanicRequestDto request) {
        PanicButtonPress press = PanicRequestMapper.toDomain(request);
        triggerPanicButtonUseCase.trigger(press);

        PanicResponseDto body = new PanicResponseDto(
                press.plate().value(), "TRIGGERED", "Alerta de pánico registrada");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(body);
    }
}
