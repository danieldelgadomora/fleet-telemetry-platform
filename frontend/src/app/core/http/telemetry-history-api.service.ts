import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { APP_CONFIG } from '../config/app-config.token';
import {
  mapTelemetryHistoryPointDto,
  TelemetryHistoryPoint,
  TelemetryHistoryPointDto,
} from '../models/telemetry-history.model';

/**
 * Envuelve el `HttpClient` contra `/api/v1/telemetry/{plate}/history` de ingestion-service —
 * un backend distinto de `apiBaseUrl` (fleet-gateway-service), por eso vive en su propio
 * servicio en vez de agregarse a `VehicleApiService`/`AlertApiService`.
 */
@Injectable({ providedIn: 'root' })
export class TelemetryHistoryApiService {
  private readonly http = inject(HttpClient);
  private readonly config = inject(APP_CONFIG);

  /** Las `limit` lecturas más recientes de la placa, en orden cronológico (la más vieja primero). */
  history(plate: string, limit = 200): Observable<TelemetryHistoryPoint[]> {
    return this.http
      .get<TelemetryHistoryPointDto[]>(`${this.config.ingestionBaseUrl}/telemetry/${encodeURIComponent(plate)}/history`, {
        params: { limit },
      })
      .pipe(map((dtos) => dtos.map(mapTelemetryHistoryPointDto)));
  }
}
