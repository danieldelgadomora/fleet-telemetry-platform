package com.simon.fleet.alerting.domain.rule;

import com.simon.fleet.alerting.domain.model.Alert;
import com.simon.fleet.alerting.domain.model.Coordinates;
import com.simon.fleet.alerting.domain.model.SafeZone;
import com.simon.fleet.alerting.domain.model.VehiclePlate;
import com.simon.fleet.alerting.domain.model.VehicleReading;
import com.simon.fleet.alerting.domain.model.VehicleTrackingState;
import com.simon.fleet.alerting.domain.port.out.GeofenceRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SafeZoneAwareAlertRuleTest {

    @Mock
    private GeofenceRepositoryPort geofenceRepositoryPort;

    private final VehiclePlate plate = new VehiclePlate("ABC123");
    private final Coordinates coordinates = new Coordinates(4.6, -74.08);
    private final VehicleReading reading = new VehicleReading(plate, coordinates, Instant.now());
    private final VehicleTrackingState updatedState = new VehicleTrackingState(plate, coordinates, Instant.now());

    @Test
    @DisplayName("si el delegado no genera alerta, nunca consulta las zonas seguras (short-circuit)")
    void delegadoSinAlertaNuncaConsultaZonasSeguras() {
        AlertRule delegate = mock(AlertRule.class);
        AlertEvaluationResult sinAlerta = new AlertEvaluationResult(updatedState, Optional.empty());
        when(delegate.evaluate(reading, Optional.empty())).thenReturn(sinAlerta);

        SafeZoneAwareAlertRule decorated = new SafeZoneAwareAlertRule(delegate, geofenceRepositoryPort);
        AlertEvaluationResult result = decorated.evaluate(reading, Optional.empty());

        assertThat(result).isEqualTo(sinAlerta);
        verifyNoInteractions(geofenceRepositoryPort);
    }

    @Test
    @DisplayName("una alerta del delegado se suprime si la coordenada cae dentro de una zona segura activa")
    void alertaSuprimidaDentroDeZonaSegura() {
        Alert alert = Alert.builder()
                .alertId("alert-1").plate(plate).ruleCode("STOPPED_VEHICLE")
                .message("detenido").raisedAt(Instant.now()).build();
        AlertEvaluationResult conAlerta = new AlertEvaluationResult(updatedState, Optional.of(alert));

        AlertRule delegate = mock(AlertRule.class);
        when(delegate.evaluate(reading, Optional.empty())).thenReturn(conAlerta);

        SafeZone zonaQueContiene = new SafeZone(1L, "Parqueadero", coordinates, 50.0);
        when(geofenceRepositoryPort.findAllActive()).thenReturn(List.of(zonaQueContiene));

        SafeZoneAwareAlertRule decorated = new SafeZoneAwareAlertRule(delegate, geofenceRepositoryPort);
        AlertEvaluationResult result = decorated.evaluate(reading, Optional.empty());

        assertThat(result.alert()).isEmpty();
        assertThat(result.updatedState()).isEqualTo(updatedState);
    }

    @Test
    @DisplayName("una alerta del delegado se propaga intacta si la coordenada está fuera de cualquier zona segura")
    void alertaPropagadaFueraDeZonaSegura() {
        Alert alert = Alert.builder()
                .alertId("alert-1").plate(plate).ruleCode("STOPPED_VEHICLE")
                .message("detenido").raisedAt(Instant.now()).build();
        AlertEvaluationResult conAlerta = new AlertEvaluationResult(updatedState, Optional.of(alert));

        AlertRule delegate = mock(AlertRule.class);
        when(delegate.evaluate(reading, Optional.empty())).thenReturn(conAlerta);

        SafeZone zonaLejana = new SafeZone(1L, "Otro parqueadero", new Coordinates(0.0, 0.0), 50.0);
        when(geofenceRepositoryPort.findAllActive()).thenReturn(List.of(zonaLejana));

        SafeZoneAwareAlertRule decorated = new SafeZoneAwareAlertRule(delegate, geofenceRepositoryPort);
        AlertEvaluationResult result = decorated.evaluate(reading, Optional.empty());

        assertThat(result).isEqualTo(conAlerta);
    }

    @Test
    @DisplayName("ruleCode() delega en la regla envuelta")
    void ruleCodeDelegaEnLaReglaEnvuelta() {
        AlertRule delegate = mock(AlertRule.class);
        when(delegate.ruleCode()).thenReturn("STOPPED_VEHICLE");

        SafeZoneAwareAlertRule decorated = new SafeZoneAwareAlertRule(delegate, geofenceRepositoryPort);

        assertThat(decorated.ruleCode()).isEqualTo("STOPPED_VEHICLE");
    }
}
