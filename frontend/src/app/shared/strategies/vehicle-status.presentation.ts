import { VehicleStatus } from '../../core/models/movement-status.model';
import { StatusPresentation } from './status-presentation.model';

const PRESENTATIONS: Record<VehicleStatus, StatusPresentation> = {
  ACTIVE: { label: 'Activo', icon: 'check_circle', cssClass: 'status-moving' },
  PENDING_DELETION: { label: 'Eliminando…', icon: 'hourglass_top', cssClass: 'status-serious' },
  DELETED: { label: 'Eliminado', icon: 'delete', cssClass: 'status-unknown' },
};

/**
 * Traduce el `status` del ciclo de vida del vehículo (Saga de eliminación) a su representación
 * visual. `PENDING_DELETION` es el caso relevante para la UI: el vehículo sigue en el listado
 * un instante mientras la Saga confirma, y debe leerse como "en proceso", no como un error.
 */
export function presentVehicleStatus(status: VehicleStatus): StatusPresentation {
  return PRESENTATIONS[status];
}
