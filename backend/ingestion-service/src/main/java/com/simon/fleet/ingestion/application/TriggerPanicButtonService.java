package com.simon.fleet.ingestion.application;

import com.simon.fleet.ingestion.domain.model.PanicButtonPress;
import com.simon.fleet.ingestion.domain.port.in.TriggerPanicButtonUseCase;
import com.simon.fleet.ingestion.domain.port.out.PanicEventPublisherPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Orquesta la activación del botón de pánico: a diferencia de la ingesta de telemetría, no hay
 * validación de negocio adicional ni deduplicación que aplicar aquí (ya la resolvió
 * {@code PanicRequestMapper}), así que el único paso es publicar el evento para que
 * alerting-service genere la alerta correspondiente.
 */
@Service
@RequiredArgsConstructor
public class TriggerPanicButtonService implements TriggerPanicButtonUseCase {

    private final PanicEventPublisherPort eventPublisherPort;

    /** Publica la activación del botón de pánico para que alerting-service genere la alerta. */
    @Override
    public void trigger(PanicButtonPress press) {
        eventPublisherPort.publishTriggered(press);
    }
}
