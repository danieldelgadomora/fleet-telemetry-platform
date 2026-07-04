package com.simon.fleet.gateway.application;

import com.simon.fleet.gateway.domain.model.Alert;
import com.simon.fleet.gateway.domain.port.in.ListAlertsUseCase;
import com.simon.fleet.gateway.domain.port.out.AlertRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListAlertsService implements ListAlertsUseCase {

    private final AlertRepositoryPort alertRepositoryPort;

    @Override
    public List<Alert> listRecent(int limit) {
        return alertRepositoryPort.findRecent(limit);
    }
}
