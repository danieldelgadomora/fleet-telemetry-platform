package com.simon.fleet.ingestion.domain.port.in;

import com.simon.fleet.ingestion.domain.model.TelemetryPoint;
import com.simon.fleet.ingestion.domain.model.VehiclePlate;

import java.util.List;

/**
 * Puerto de entrada (driving): expone el histórico reciente de un vehículo para que el
 * dashboard pueda trazar su recorrido en el mapa. A diferencia de {@link IngestTelemetryUseCase},
 * es puramente de lectura y no participa del camino rápido de la ingesta.
 */
public interface GetTelemetryHistoryUseCase {

    /**
     * Las {@code limit} lecturas más recientes de la placa, en orden cronológico (la más vieja
     * primero, para que un consumidor pueda dibujarlas directamente como una ruta sin tener que
     * invertir el orden él mismo). Lista vacía si la placa nunca reportó telemetría.
     */
    List<TelemetryPoint> history(VehiclePlate plate, int limit);
}
