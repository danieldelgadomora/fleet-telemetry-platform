import * as L from 'leaflet';
import { Vehicle } from '../../../core/models/vehicle.model';
import { presentMovementStatus } from '../../../shared/strategies/movement-status.presentation';

/**
 * Construye y actualiza los marcadores Leaflet de un vehículo paso a paso (posición, ícono,
 * contenido del popup), traduciendo su `movement_status` mediante
 * {@link presentMovementStatus} — el componente de mapa nunca arma un ícono de Leaflet a mano.
 */
export class MarkerBuilder {
  /** Crea un marcador nuevo, ya posicionado y con su ícono/popup iniciales. */
  static build(vehicle: Vehicle & { lastLat: number; lastLng: number }): L.Marker {
    return L.marker([vehicle.lastLat, vehicle.lastLng], { icon: MarkerBuilder.iconFor(vehicle) }).bindPopup(
      MarkerBuilder.popupFor(vehicle),
    );
  }

  /** Ícono del marcador según el `movement_status` actual del vehículo. */
  static iconFor(vehicle: Vehicle): L.DivIcon {
    const presentation = presentMovementStatus(vehicle.movementStatus);
    return L.divIcon({
      className: 'vehicle-marker',
      html: `<span class="material-icons ${presentation.cssClass}">${presentation.icon}</span>`,
      iconSize: [30, 30],
      iconAnchor: [15, 15],
    });
  }

  /** Contenido del popup que se abre al hacer clic sobre el marcador. */
  static popupFor(vehicle: Vehicle): string {
    const presentation = presentMovementStatus(vehicle.movementStatus);
    return `<strong>${vehicle.vehicleId}</strong><br>${presentation.label}`;
  }
}
