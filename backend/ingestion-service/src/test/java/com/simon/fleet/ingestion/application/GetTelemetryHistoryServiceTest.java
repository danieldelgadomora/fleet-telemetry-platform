package com.simon.fleet.ingestion.application;

import com.simon.fleet.ingestion.domain.model.Coordinates;
import com.simon.fleet.ingestion.domain.model.TelemetryPoint;
import com.simon.fleet.ingestion.domain.model.VehiclePlate;
import com.simon.fleet.ingestion.domain.port.out.TelemetryHistoryRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTelemetryHistoryServiceTest {

    @Mock
    private TelemetryHistoryRepositoryPort historyRepositoryPort;

    @InjectMocks
    private GetTelemetryHistoryService service;

    private final VehiclePlate plate = new VehiclePlate("ABC123");

    @Test
    @DisplayName("delega placa y límite al puerto de salida")
    void delegaPlacaYLimiteAlPuerto() {
        when(historyRepositoryPort.findRecent(plate, 200)).thenReturn(List.of());

        service.history(plate, 200);

        verify(historyRepositoryPort).findRecent(plate, 200);
    }

    @Test
    @DisplayName("invierte el orden más-reciente-primero del puerto a orden cronológico")
    void invierteOrdenAMasCronologico() {
        TelemetryPoint p1 = pointAt(Instant.parse("2026-07-03T21:00:00Z"));
        TelemetryPoint p2 = pointAt(Instant.parse("2026-07-03T21:00:15Z"));
        TelemetryPoint p3 = pointAt(Instant.parse("2026-07-03T21:00:30Z"));
        when(historyRepositoryPort.findRecent(plate, 200)).thenReturn(List.of(p3, p2, p1));

        List<TelemetryPoint> result = service.history(plate, 200);

        assertThat(result).containsExactly(p1, p2, p3);
    }

    private TelemetryPoint pointAt(Instant recordedAt) {
        return new TelemetryPoint(plate, new Coordinates(4.6, -74.08), recordedAt);
    }
}
