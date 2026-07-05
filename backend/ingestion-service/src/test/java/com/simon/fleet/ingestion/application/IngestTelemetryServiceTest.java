package com.simon.fleet.ingestion.application;

import com.simon.fleet.ingestion.domain.exception.InvalidTelemetryPayloadException;
import com.simon.fleet.ingestion.domain.model.Coordinates;
import com.simon.fleet.ingestion.domain.model.TelemetryIngestionResult;
import com.simon.fleet.ingestion.domain.model.TelemetryPoint;
import com.simon.fleet.ingestion.domain.model.VehiclePlate;
import com.simon.fleet.ingestion.domain.port.out.TelemetryCachePort;
import com.simon.fleet.ingestion.domain.port.out.TelemetryDeduplicationPort;
import com.simon.fleet.ingestion.domain.port.out.TelemetryEventPublisherPort;
import com.simon.fleet.ingestion.domain.validation.TelemetryValidationChain;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IngestTelemetryServiceTest {

    @Mock
    private TelemetryValidationChain validationChain;
    @Mock
    private TelemetryDeduplicationPort deduplicationPort;
    @Mock
    private TelemetryCachePort cachePort;
    @Mock
    private TelemetryEventPublisherPort eventPublisherPort;

    @InjectMocks
    private IngestTelemetryService service;

    private final TelemetryPoint point =
            new TelemetryPoint(new VehiclePlate("ABC123"), new Coordinates(4.6, -74.08), Instant.now());

    @Test
    @DisplayName("una lectura válida y no duplicada se cachea, se publica y responde ACCEPTED")
    void lecturaValidaYNoDuplicadaEsAceptada() {
        when(deduplicationPort.isDuplicate(point)).thenReturn(false);

        TelemetryIngestionResult result = service.ingest(point);

        assertThat(result).isEqualTo(TelemetryIngestionResult.ACCEPTED);
        verify(cachePort).saveLastKnownPosition(point);
        verify(eventPublisherPort).publishReceived(point);
    }

    @Test
    @DisplayName("una lectura duplicada se ignora y nunca se cachea ni se publica")
    void lecturaDuplicadaSeIgnoraSinCachearNiPublicar() {
        when(deduplicationPort.isDuplicate(point)).thenReturn(true);

        TelemetryIngestionResult result = service.ingest(point);

        assertThat(result).isEqualTo(TelemetryIngestionResult.DUPLICATE_IGNORED);
        verify(cachePort, never()).saveLastKnownPosition(point);
        verify(eventPublisherPort, never()).publishReceived(point);
    }

    @Test
    @DisplayName("si la validación falla, la excepción se propaga sin tocar dedupe, cache ni publish")
    void validacionFallidaPropagaExcepcionSinEfectosSecundarios() {
        doThrow(new InvalidTelemetryPayloadException("payload inválido"))
                .when(validationChain).validate(point);

        assertThatThrownBy(() -> service.ingest(point))
                .isInstanceOf(InvalidTelemetryPayloadException.class);

        verifyNoInteractions(deduplicationPort, cachePort, eventPublisherPort);
    }
}
