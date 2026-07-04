import { MovementStatus } from '../../core/models/movement-status.model';
import { StatusPresentation } from './status-presentation.model';

const PRESENTATIONS: Record<MovementStatus, StatusPresentation> = {
  EN_MOVIMIENTO: { label: 'En movimiento', icon: 'directions_car', cssClass: 'status-moving' },
  DETENIDO: { label: 'Detenido', icon: 'pause_circle', cssClass: 'status-stopped' },
  ALERTA: { label: 'Alerta', icon: 'warning', cssClass: 'status-alert' },
};

const SIN_DATOS: StatusPresentation = { label: 'Sin datos', icon: 'help_outline', cssClass: 'status-unknown' };

/**
 * Traduce el `movement_status` de un vehículo a su representación visual (ícono, etiqueta y
 * clase de color). Es el único lugar donde vive esa correspondencia: agregar un estado nuevo
 * solo requiere extender este mapa, sin tocar los componentes que lo consumen.
 */
export function presentMovementStatus(status: MovementStatus | null): StatusPresentation {
  return status ? PRESENTATIONS[status] : SIN_DATOS;
}
