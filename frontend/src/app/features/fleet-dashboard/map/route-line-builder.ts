import * as L from 'leaflet';
import { TelemetryHistoryPoint } from '../../../core/models/telemetry-history.model';

/**
 * Construye la polyline de recorrido de un vehículo a partir de su historial, ya en orden
 * cronológico — hermano de {@link MarkerBuilder}, mismo motivo: el componente de mapa nunca
 * arma una capa de Leaflet a mano.
 */
export class RouteLineBuilder {
  private static readonly STYLE: L.PolylineOptions = { color: '#5ec6c2', weight: 3, opacity: 0.8 };

  /** Crea la polyline ya posicionada sobre los puntos del historial. */
  static build(points: TelemetryHistoryPoint[]): L.Polyline {
    return L.polyline(RouteLineBuilder.toLatLngs(points), RouteLineBuilder.STYLE);
  }

  static toLatLngs(points: TelemetryHistoryPoint[]): L.LatLngExpression[] {
    return points.map((point) => [point.lat, point.lng]);
  }
}
