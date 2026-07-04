import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { APP_CONFIG } from '../config/app-config.token';
import { Alert, AlertDto, mapAlertDto } from '../models/alert.model';

/** Envuelve el `HttpClient` contra `/api/v1/alerts` (historial reciente de alertas). */
@Injectable({ providedIn: 'root' })
export class AlertApiService {
  private readonly http = inject(HttpClient);
  private readonly config = inject(APP_CONFIG);

  /** Las `limit` alertas más recientes de toda la flota, la más nueva primero. */
  listRecent(limit = 50): Observable<Alert[]> {
    return this.http
      .get<AlertDto[]>(`${this.config.apiBaseUrl}/alerts`, { params: { limit } })
      .pipe(map((dtos) => dtos.map(mapAlertDto)));
  }
}
