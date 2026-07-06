import { ChangeDetectionStrategy, Component, effect, inject } from '@angular/core';
import { LeafletModule } from '@bluehalo/ngx-leaflet';
import * as L from 'leaflet';
import { latLng, tileLayer } from 'leaflet';
import { FleetStoreService } from '../../../core/store/fleet-store.service';
import { TelemetryHistoryApiService } from '../../../core/http/telemetry-history-api.service';
import { hasPosition, Vehicle } from '../../../core/models/vehicle.model';
import { MovementStatus } from '../../../core/models/movement-status.model';
import { MarkerBuilder } from './marker-builder';
import { RouteLineBuilder } from './route-line-builder';

interface MarkerEntry {
  marker: L.Marker;
  movementStatus: MovementStatus | null;
}

/**
 * Mapa en vivo de la flota. Crea la instancia de Leaflet una única vez y sincroniza los
 * marcadores por diferencia contra el listado de vehículos, sin recrear el mapa ni los
 * marcadores en cada actualización que llega por WebSocket.
 */
@Component({
  selector: 'app-fleet-map',
  standalone: true,
  imports: [LeafletModule],
  templateUrl: './fleet-map.component.html',
  styleUrl: './fleet-map.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FleetMapComponent {
  private readonly fleetStore = inject(FleetStoreService);
  private readonly telemetryHistoryApi = inject(TelemetryHistoryApiService);
  private readonly markersByPlate = new Map<string, MarkerEntry>();
  private map?: L.Map;
  private routeLine?: L.Polyline;
  /** Última placa para la que ya se pidió/está en vuelo el historial — evita re-pedirlo en cada
   *  tick de WebSocket, ya que el efecto de abajo se re-ejecuta en cada actualización de
   *  `vehicles()`, no solo cuando cambia `selectedPlate()`. Es una preocupación de presentación
   *  (no repetir peticiones), no estado de dominio, por eso vive aquí y no en el store. */
  private lastFetchedRoutePlate: string | null = null;

  /** Capa oscura de tiles (sin API key) y vista inicial centrada en Bogotá. */
  readonly mapOptions: L.MapOptions = {
    layers: [
      tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
        maxZoom: 19,
        attribution: '&copy; OpenStreetMap contributors &copy; CARTO',
      }),
    ],
    zoom: 6,
    center: latLng(4.6, -74.08),
  };

  constructor() {
    // Un único efecto: primero sincroniza los marcadores y luego enfoca el seleccionado, para
    // garantizar que el marcador ya exista antes de intentar centrar el mapa en él (si fueran
    // dos efectos separados, el de foco podría ejecutarse antes de que el de sync creara el
    // marcador de un vehículo recién aparecido).
    effect(() => {
      const vehicles = this.fleetStore.vehicles();
      const selectedPlate = this.fleetStore.selectedPlate();
      this.syncMarkers(vehicles);
      this.focusSelected(selectedPlate, vehicles);
      if (selectedPlate !== this.lastFetchedRoutePlate) {
        this.loadRoute(selectedPlate);
      }
    });
  }

  /** Guarda la instancia del mapa apenas Leaflet termina de crearla. */
  onMapReady(map: L.Map): void {
    this.map = map;
  }

  /**
   * Difiere el listado de vehículos contra los marcadores ya dibujados: agrega los nuevos,
   * mueve los existentes a su posición más reciente, solo recalcula el ícono cuando el
   * `movement_status` cambió, y retira los marcadores de vehículos que ya no están en el
   * listado (llegaron a `DELETED`).
   */
  private syncMarkers(vehicles: Vehicle[]): void {
    if (!this.map) {
      return;
    }
    const idsPresentes = new Set(vehicles.map((v) => v.plate));
    for (const [plate, entry] of this.markersByPlate) {
      if (!idsPresentes.has(plate)) {
        entry.marker.remove();
        this.markersByPlate.delete(plate);
      }
    }

    for (const vehicle of vehicles) {
      if (!hasPosition(vehicle)) {
        continue;
      }
      const entry = this.markersByPlate.get(vehicle.plate);
      if (!entry) {
        const marker = MarkerBuilder.build(vehicle);
        marker.addTo(this.map);
        this.markersByPlate.set(vehicle.plate, { marker, movementStatus: vehicle.movementStatus });
        continue;
      }
      entry.marker.setLatLng([vehicle.lastLat, vehicle.lastLng]);
      entry.marker.setPopupContent(MarkerBuilder.popupFor(vehicle));
      if (entry.movementStatus !== vehicle.movementStatus) {
        entry.marker.setIcon(MarkerBuilder.iconFor(vehicle));
        entry.movementStatus = vehicle.movementStatus;
      }
    }
  }

  /**
   * Centra el mapa en el vehículo seleccionado y abre su popup. Si no hay selección o el
   * vehículo seleccionado no tiene posición reportada, cierra cualquier popup abierto en vez de
   * dejar visible la información de una selección anterior.
   */
  private focusSelected(plate: string | null, vehicles: Vehicle[]): void {
    if (!this.map) {
      return;
    }
    const vehicle = plate ? vehicles.find((v) => v.plate === plate) : undefined;
    const entry = plate ? this.markersByPlate.get(plate) : undefined;

    if (!vehicle || !hasPosition(vehicle) || !entry) {
      this.map.closePopup();
      return;
    }
    this.map.flyTo([vehicle.lastLat, vehicle.lastLng], Math.max(this.map.getZoom(), 14));
    entry.marker.openPopup();
  }

  /**
   * Traza (o quita) la ruta del vehículo seleccionado. Marca `plate` como ya pedida de
   * inmediato, antes de que resuelva el HTTP, para no disparar una segunda petición si el
   * efecto se re-ejecuta mientras la primera sigue en vuelo. Si el historial falla (ej.
   * ingestion-service caído), se descarta en silencio: es una funcionalidad secundaria y el
   * resto del dashboard sigue funcionando con los datos de fleet-gateway-service.
   */
  private loadRoute(plate: string | null): void {
    this.lastFetchedRoutePlate = plate;
    this.routeLine?.remove();
    this.routeLine = undefined;

    if (!plate || !this.map) {
      return;
    }
    this.telemetryHistoryApi.history(plate).subscribe({
      next: (points) => {
        if (plate !== this.lastFetchedRoutePlate || !this.map) {
          return;
        }
        this.routeLine = RouteLineBuilder.build(points).addTo(this.map);
      },
      error: () => {
        // Funcionalidad secundaria: sin ruta, sin toast bloqueante.
      },
    });
  }
}
