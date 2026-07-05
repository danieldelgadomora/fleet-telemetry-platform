package com.simon.fleet.ingestion.domain.port.in;

import com.simon.fleet.ingestion.domain.exception.InvalidPanicPayloadException;
import com.simon.fleet.ingestion.domain.model.PanicButtonPress;

/**
 * Puerto de entrada (driving): caso de uso invocado por {@code PanicController} cuando el
 * conductor presiona el botón de pánico desde la app móvil. A diferencia de la ingesta de
 * telemetría, no hay validación de coordenadas ni deduplicación: cada activación es un hecho
 * único que siempre debe propagarse.
 */
public interface TriggerPanicButtonUseCase {

    /**
     * @throws InvalidPanicPayloadException si el payload no cumple sus reglas mínimas.
     */
    void trigger(PanicButtonPress press);
}
