package com.simon.fleet.gateway.application;

import com.simon.fleet.gateway.domain.model.Alert;
import com.simon.fleet.gateway.domain.port.in.ListAlertsUseCase;
import com.simon.fleet.gateway.domain.port.out.AlertRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Historial reciente de alertas de la flota, para que el dashboard no dependa únicamente del
 * push en vivo por WebSocket al abrir o recargar la página.
 */
@Service
@RequiredArgsConstructor
public class ListAlertsService implements ListAlertsUseCase {

    private final AlertRepositoryPort alertRepositoryPort;

    /** Devuelve las {@code limit} alertas más recientes de toda la flota. */
    @Override
    public List<Alert> listRecent(int limit) {
        return alertRepositoryPort.findRecent(limit);
    }
}
