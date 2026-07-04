import { computed, inject, Injectable, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { Alert, AlertDto, mapAlertDto } from '../models/alert.model';
import { mapVehicleDto, Vehicle, VehicleDto } from '../models/vehicle.model';
import { VehicleApiService } from '../http/vehicle-api.service';
import { REALTIME_CLIENT } from '../realtime/realtime-client.token';

/** Cantidad máxima de alertas recientes que se conservan en memoria para el panel de alertas. */
const MAX_ALERTS_EN_MEMORIA = 50;

/** Conteo de vehículos por estado de movimiento, para las stat-tiles del encabezado. */
export interface FleetStats {
  total: number;
  enMovimiento: number;
  detenido: number;
  alerta: number;
}

/**
 * Fachada del estado de la flota: combina el `VehicleApiService` (REST) y el
 * `RealtimeClientPort` (WebSocket/STOMP) detrás de signals de solo lectura, para que los
 * componentes de presentación nunca dependan directamente de HTTP ni de STOMP.
 */
@Injectable({ providedIn: 'root' })
export class FleetStoreService {
  private readonly vehicleApi = inject(VehicleApiService);
  private readonly realtimeClient = inject(REALTIME_CLIENT);

  private readonly _vehicles = signal<Vehicle[]>([]);
  private readonly _alerts = signal<Alert[]>([]);
  private readonly _selectedVehicleId = signal<string | null>(null);

  /** Vehículos activos y su último estado conocido. */
  readonly vehicles = this._vehicles.asReadonly();
  /** Alertas recientes, la más nueva primero. */
  readonly alerts = this._alerts.asReadonly();
  /** Estado de la conexión en tiempo real, para el indicador de conexión del header. */
  readonly connectionStatus = this.realtimeClient.connectionStatus;
  /** Id del vehículo seleccionado en el listado, para enfocarlo en el mapa y ver su detalle. */
  readonly selectedVehicleId = this._selectedVehicleId.asReadonly();
  /** Vehículo seleccionado completo, o `null` si no hay selección o ya no existe en el listado. */
  readonly selectedVehicle = computed(
    () => this._vehicles().find((v) => v.vehicleId === this._selectedVehicleId()) ?? null,
  );

  /** Conteo de vehículos por estado de movimiento. */
  readonly stats = computed<FleetStats>(() => {
    const vehicles = this._vehicles();
    return {
      total: vehicles.length,
      enMovimiento: vehicles.filter((v) => v.movementStatus === 'EN_MOVIMIENTO').length,
      detenido: vehicles.filter((v) => v.movementStatus === 'DETENIDO').length,
      alerta: vehicles.filter((v) => v.movementStatus === 'ALERTA').length,
    };
  });

  /**
   * Carga el listado inicial por REST y abre el canal en tiempo real. Se llama una única vez,
   * al arrancar el shell de la aplicación.
   */
  init(): void {
    this.vehicleApi.list().subscribe((vehicles) => this._vehicles.set(vehicles));
    this.realtimeClient.subscribe<VehicleDto>('/topic/fleet', (dto) =>
      this.applyVehicleUpdate(mapVehicleDto(dto)),
    );
    this.realtimeClient.subscribe<AlertDto>('/topic/alerts', (dto) => this.applyAlert(mapAlertDto(dto)));
    this.realtimeClient.connect();
  }

  /** Da de alta un vehículo y lo refleja de inmediato en el listado, sin esperar al push. */
  registerVehicle(vehicleId: string): Observable<Vehicle> {
    return this.vehicleApi.register(vehicleId).pipe(tap((vehicle) => this.applyVehicleUpdate(vehicle)));
  }

  /** Pide la eliminación de un vehículo; queda en `PENDING_DELETION` hasta que la Saga confirme. */
  removeVehicle(vehicleId: string): Observable<Vehicle> {
    return this.vehicleApi.remove(vehicleId).pipe(tap((vehicle) => this.applyVehicleUpdate(vehicle)));
  }

  /** Selecciona un vehículo del listado (o `null` para quitar la selección). */
  selectVehicle(vehicleId: string | null): void {
    this._selectedVehicleId.set(vehicleId);
  }

  /**
   * Inserta o actualiza un vehículo en el listado según su id. Cuando el vehículo llega en
   * estado `DELETED` (confirmación final de la Saga de eliminación) se retira del listado en
   * vez de mostrarse como una fila más.
   */
  private applyVehicleUpdate(vehicle: Vehicle): void {
    if (vehicle.status === 'DELETED') {
      this._vehicles.update((list) => list.filter((v) => v.vehicleId !== vehicle.vehicleId));
      if (this._selectedVehicleId() === vehicle.vehicleId) {
        this._selectedVehicleId.set(null);
      }
      return;
    }
    this._vehicles.update((list) => {
      const index = list.findIndex((v) => v.vehicleId === vehicle.vehicleId);
      if (index === -1) {
        return [...list, vehicle];
      }
      const copy = [...list];
      copy[index] = vehicle;
      return copy;
    });
  }

  /** Agrega una alerta al frente de la lista, manteniendo como máximo las más recientes. */
  private applyAlert(alert: Alert): void {
    this._alerts.update((list) => [alert, ...list].slice(0, MAX_ALERTS_EN_MEMORIA));
  }
}
