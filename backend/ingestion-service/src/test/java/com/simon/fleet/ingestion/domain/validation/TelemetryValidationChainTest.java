package com.simon.fleet.ingestion.domain.validation;

import com.simon.fleet.ingestion.domain.exception.InvalidTelemetryPayloadException;
import com.simon.fleet.ingestion.domain.model.Coordinates;
import com.simon.fleet.ingestion.domain.model.TelemetryPoint;
import com.simon.fleet.ingestion.domain.model.VehiclePlate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TelemetryValidationChainTest {

    private final TelemetryPoint point =
            new TelemetryPoint(new VehiclePlate("ABC123"), new Coordinates(4.6, -74.08), Instant.now());

    @Test
    @DisplayName("si todas las especificaciones se satisfacen, la cadena no lanza ninguna excepción")
    void todasLasEspecificacionesSatisfechasNoLanzaExcepcion() {
        TelemetrySpecification satisfecha = mock(TelemetrySpecification.class);
        when(satisfecha.isSatisfiedBy(point)).thenReturn(true);

        TelemetryValidationChain chain = new TelemetryValidationChain(List.of(satisfecha));

        chain.validate(point);
    }

    @Test
    @DisplayName("si la primera especificación falla, la cadena corta y la siguiente nunca se evalúa")
    void primeraEspecificacionFallidaCortaLaCadena() {
        TelemetrySpecification fallida = mock(TelemetrySpecification.class);
        when(fallida.isSatisfiedBy(point)).thenReturn(false);
        when(fallida.violationMessage()).thenReturn("regla incumplida");

        TelemetrySpecification nuncaEvaluada = mock(TelemetrySpecification.class);

        TelemetryValidationChain chain = new TelemetryValidationChain(List.of(fallida, nuncaEvaluada));

        assertThatThrownBy(() -> chain.validate(point))
                .isInstanceOf(InvalidTelemetryPayloadException.class)
                .hasMessage("regla incumplida");

        verify(nuncaEvaluada, never()).isSatisfiedBy(point);
    }
}
