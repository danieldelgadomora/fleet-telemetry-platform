import { computed, inject, Injectable, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { Alert, AlertDto, mapAlertDto } from '../models/alert.model';
import { mapVehicleDto, Vehicle, VehicleDto } from '../models/vehicle.model';
import { AlertApiService } from '../http/alert-api.service';
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
  private readonly alertApi = inject(AlertApiService);
  private readonly realtimeClient = inject(REALTIME_CLIENT);

  private readonly _vehicles = signal<Vehicle[]>([]);
  private readonly _alerts = signal<Alert[]>([]);
  private readonly _selectedPlate = signal<string | null>(null);

  /** Vehículos activos y su último estado conocido. */
  readonly vehicles = this._vehicles.asReadonly();
  /** Alertas recientes, la más nueva primero. */
  readonly alerts = this._alerts.asReadonly();
  /** Estado de la conexión en tiempo real, para el indicador de conexión del header. */
  readonly connectionStatus = this.realtimeClient.connectionStatus;
  /** Placa del vehículo seleccionado en el listado, para enfocarlo en el mapa y ver su detalle. */
  readonly selectedPlate = this._selectedPlate.asReadonly();
  /** Vehículo seleccionado completo, o `null` si no hay selección o ya no existe en el listado. */
  readonly selectedVehicle = computed(
    () => this._vehicles().find((v) => v.plate === this._selectedPlate()) ?? null,
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
   * Carga el listado inicial por REST (vehículos y alertas recientes) y abre el canal en
   * tiempo real. Se llama una única vez, al arrancar el shell de la aplicación — sin la carga
   * inicial de alertas, el panel arrancaría vacío en cada recarga hasta la próxima alerta en
   * vivo, aunque ya existan alertas recientes en el historial.
   */
  init(): void {
    this.vehicleApi.list().subscribe((vehicles) => this._vehicles.set(vehicles));
    this.alertApi.listRecent().subscribe((alerts) => this._alerts.set(alerts));
    this.realtimeClient.subscribe<VehicleDto>('/topic/fleet', (dto) =>
      this.applyVehicleUpdate(mapVehicleDto(dto)),
    );
    this.realtimeClient.subscribe<AlertDto>('/topic/alerts', (dto) => this.applyAlert(mapAlertDto(dto)));
    this.realtimeClient.connect();
  }

  /** Da de alta un vehículo y lo refleja de inmediato en el listado, sin esperar al push. */
  registerVehicle(plate: string): Observable<Vehicle> {
    return this.vehicleApi.register(plate).pipe(tap((vehicle) => this.applyVehicleUpdate(vehicle)));
  }

  /** Pide la eliminación de un vehículo; queda en `PENDING_DELETION` hasta que la Saga confirme. */
  removeVehicle(plate: string): Observable<Vehicle> {
    return this.vehicleApi.remove(plate).pipe(tap((vehicle) => this.applyVehicleUpdate(vehicle)));
  }

  /** Selecciona un vehículo del listado (o `null` para quitar la selección). */
  selectVehicle(plate: string | null): void {
    this._selectedPlate.set(plate);
  }

  /**
   * Inserta o actualiza un vehículo en el listado según su placa. Cuando el vehículo llega en
   * estado `DELETED` (confirmación final de la Saga de eliminación) se retira del listado en
   * vez de mostrarse como una fila más.
   */
  private applyVehicleUpdate(vehicle: Vehicle): void {
    if (vehicle.status === 'DELETED') {
      this._vehicles.update((list) => list.filter((v) => v.plate !== vehicle.plate));
      if (this._selectedPlate() === vehicle.plate) {
        this._selectedPlate.set(null);
      }
      return;
    }
    this._vehicles.update((list) => {
      const index = list.findIndex((v) => v.plate === vehicle.plate);
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
