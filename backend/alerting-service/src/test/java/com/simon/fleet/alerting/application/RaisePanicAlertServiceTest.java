package com.simon.fleet.alerting.application;

import com.simon.fleet.alerting.domain.model.Alert;
import com.simon.fleet.alerting.domain.model.VehiclePlate;
import com.simon.fleet.alerting.domain.port.out.AlertEventPublisherPort;
import com.simon.fleet.alerting.domain.port.out.AlertRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RaisePanicAlertServiceTest {

    @Mock
    private AlertRepositoryPort alertRepositoryPort;
    @Mock
    private AlertEventPublisherPort eventPublisherPort;

    @InjectMocks
    private RaisePanicAlertService service;

    @Captor
    private ArgumentCaptor<Alert> alertCaptor;

    private final VehiclePlate plate = new VehiclePlate("ABC123");
    private final Instant triggeredAt = Instant.parse("2026-07-05T12:00:00Z");

    @Test
    @DisplayName("con coordenadas y nota del conductor, el mensaje incluye ambas y ambos puertos reciben la alerta")
    void conCoordenadasYNotaElMensajeIncluyeAmbas() {
        service.raise(plate, 4.6, -74.08, "vehículo interceptado", triggeredAt);

        verify(alertRepositoryPort).save(alertCaptor.capture());
        Alert alert = alertCaptor.getValue();
        assertThat(alert.getRuleCode()).isEqualTo(RaisePanicAlertService.RULE_CODE);
        assertThat(alert.getMessage())
                .contains("ABC123").contains("4.6").contains("-74.08").contains("vehículo interceptado");
        verify(eventPublisherPort).publishRaised(alert);
    }

    @Test
    @DisplayName("sin coordenadas conocidas, el mensaje no menciona ubicación")
    void sinCoordenadasElMensajeNoMencionaUbicacion() {
        service.raise(plate, null, null, "ayuda", triggeredAt);

        verify(alertRepositoryPort).save(alertCaptor.capture());
        assertThat(alertCaptor.getValue().getMessage()).doesNotContain(" en (").contains("ayuda");
    }

    @Test
    @DisplayName("sin nota del conductor, el mensaje no incluye la parte de la nota")
    void sinNotaElMensajeNoIncluyeNota() {
        service.raise(plate, 4.6, -74.08, null, triggeredAt);

        verify(alertRepositoryPort).save(alertCaptor.capture());
        assertThat(alertCaptor.getValue().getMessage()).doesNotContain(":").contains("4.6");
    }

    @Test
    @DisplayName("sin coordenadas ni nota, el mensaje es el mínimo")
    void sinCoordenadasNiNotaElMensajeEsMinimo() {
        service.raise(plate, null, null, null, triggeredAt);

        verify(alertRepositoryPort).save(alertCaptor.capture());
        assertThat(alertCaptor.getValue().getMessage()).isEqualTo("Botón de pánico activado por ABC123");
    }
}
