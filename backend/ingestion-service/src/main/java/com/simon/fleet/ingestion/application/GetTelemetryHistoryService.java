package com.simon.fleet.ingestion.application;

import com.simon.fleet.ingestion.domain.model.TelemetryPoint;
import com.simon.fleet.ingestion.domain.model.VehiclePlate;
import com.simon.fleet.ingestion.domain.port.in.GetTelemetryHistoryUseCase;
import com.simon.fleet.ingestion.domain.port.out.TelemetryHistoryRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * El puerto de salida entrega las lecturas más recientes primero (el orden natural de "traer
 * las N últimas"); una ruta necesita dibujarse en el orden en que el vehículo realmente se
 * movió, así que este servicio invierte el orden antes de devolverlo — es la única lógica que
 * distingue a este caso de uso de un simple passthrough.
 */
@Service
@RequiredArgsConstructor
public class GetTelemetryHistoryService implements GetTelemetryHistoryUseCase {

    private final TelemetryHistoryRepositoryPort historyRepositoryPort;

    @Override
    public List<TelemetryPoint> history(VehiclePlate plate, int limit) {
        List<TelemetryPoint> chronological = new ArrayList<>(historyRepositoryPort.findRecent(plate, limit));
        Collections.reverse(chronological);
        return chronological;
    }
}
