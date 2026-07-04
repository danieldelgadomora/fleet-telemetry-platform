import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { APP_CONFIG } from '../config/app-config.token';
import { mapVehicleDto, Vehicle, VehicleDto } from '../models/vehicle.model';

/**
 * Envuelve el `HttpClient` contra `/api/v1/vehicles`, exponiendo operaciones de dominio en vez
 * de detalles de la petición HTTP — análogo al `VehicleRepositoryPort` del backend.
 */
@Injectable({ providedIn: 'root' })
export class VehicleApiService {
  private readonly http = inject(HttpClient);
  private readonly config = inject(APP_CONFIG);
  private readonly baseUrl = `${this.config.apiBaseUrl}/vehicles`;

  /** Lista los vehículos activos y su último estado conocido. */
  list(): Observable<Vehicle[]> {
    return this.http.get<VehicleDto[]>(this.baseUrl).pipe(map((dtos) => dtos.map(mapVehicleDto)));
  }

  /** Registra un vehículo nuevo. Rechaza (409, vía `ApiError`) si el id ya está registrado. */
  register(vehicleId: string): Observable<Vehicle> {
    return this.http
      .post<VehicleDto>(this.baseUrl, { vehicle_id: vehicleId })
      .pipe(map(mapVehicleDto));
  }

  /** Arranca la Saga de eliminación del vehículo; el resultado inmediato queda en `PENDING_DELETION`. */
  remove(vehicleId: string): Observable<Vehicle> {
    return this.http
      .delete<VehicleDto>(`${this.baseUrl}/${encodeURIComponent(vehicleId)}`)
      .pipe(map(mapVehicleDto));
  }
}
