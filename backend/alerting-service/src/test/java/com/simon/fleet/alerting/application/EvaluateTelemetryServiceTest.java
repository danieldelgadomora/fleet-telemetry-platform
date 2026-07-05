package com.simon.fleet.alerting.application;

import com.simon.fleet.alerting.domain.model.Alert;
import com.simon.fleet.alerting.domain.model.Coordinates;
import com.simon.fleet.alerting.domain.model.VehiclePlate;
import com.simon.fleet.alerting.domain.model.VehicleReading;
import com.simon.fleet.alerting.domain.model.VehicleTrackingState;
import com.simon.fleet.alerting.domain.port.out.AlertEventPublisherPort;
import com.simon.fleet.alerting.domain.port.out.AlertRepositoryPort;
import com.simon.fleet.alerting.domain.port.out.VehicleTrackingStatePort;
import com.simon.fleet.alerting.domain.rule.AlertEvaluationResult;
import com.simon.fleet.alerting.domain.rule.AlertRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvaluateTelemetryServiceTest {

    @Mock
    private VehicleTrackingStatePort trackingStatePort;
    @Mock
    private AlertRepositoryPort alertRepositoryPort;
    @Mock
    private AlertEventPublisherPort eventPublisherPort;

    private final VehiclePlate plate = new VehiclePlate("ABC123");
    private final VehicleReading reading = new VehicleReading(plate, new Coordinates(4.6, -74.08), Instant.now());

    @Test
    @DisplayName("evalúa todas las reglas registradas con el mismo estado inicial y persiste el estado de cada una")
    void evaluaTodasLasReglasConElMismoEstadoInicial() {
        Optional<VehicleTrackingState> estadoInicial =
                Optional.of(new VehicleTrackingState(plate, reading.coordinates(), Instant.now()));
        when(trackingStatePort.find(plate)).thenReturn(estadoInicial);

        AlertRule reglaSinAlerta = mock(AlertRule.class);
        VehicleTrackingState estadoReglaUno = new VehicleTrackingState(plate, reading.coordinates(), Instant.now());
        when(reglaSinAlerta.evaluate(reading, estadoInicial))
                .thenReturn(new AlertEvaluationResult(estadoReglaUno, Optional.empty()));

        AlertRule reglaConAlerta = mock(AlertRule.class);
        Alert alert = Alert.builder()
                .alertId("alert-1").plate(plate).ruleCode("STOPPED_VEHICLE")
                .message("detenido").raisedAt(Instant.now()).build();
        VehicleTrackingState estadoReglaDos = new VehicleTrackingState(plate, reading.coordinates(), Instant.now());
        when(reglaConAlerta.evaluate(reading, estadoInicial))
                .thenReturn(new AlertEvaluationResult(estadoReglaDos, Optional.of(alert)));

        EvaluateTelemetryService service = new EvaluateTelemetryService(
                List.of(reglaSinAlerta, reglaConAlerta), trackingStatePort, alertRepositoryPort, eventPublisherPort);

        service.evaluate(reading);

        verify(trackingStatePort).find(plate);
        verify(trackingStatePort).save(estadoReglaUno);
        verify(trackingStatePort).save(estadoReglaDos);
        verify(alertRepositoryPort, times(1)).save(alert);
        verify(eventPublisherPort, times(1)).publishRaised(alert);
    }

    @Test
    @DisplayName("si ninguna regla genera alerta, nunca se persiste ni se publica ninguna alerta")
    void ningunaReglaGeneraAlertaNuncaPersisteNiPublica() {
        when(trackingStatePort.find(plate)).thenReturn(Optional.empty());

        AlertRule regla = mock(AlertRule.class);
        VehicleTrackingState nuevoEstado = new VehicleTrackingState(plate, reading.coordinates(), Instant.now());
        when(regla.evaluate(reading, Optional.empty()))
                .thenReturn(new AlertEvaluationResult(nuevoEstado, Optional.empty()));

        EvaluateTelemetryService service = new EvaluateTelemetryService(
                List.of(regla), trackingStatePort, alertRepositoryPort, eventPublisherPort);

        service.evaluate(reading);

        verify(trackingStatePort).save(nuevoEstado);
        verifyNoInteractions(alertRepositoryPort, eventPublisherPort);
    }
}
