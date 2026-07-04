import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { hasPosition, Vehicle } from '../../../../core/models/vehicle.model';
import { RelativeTimePipe } from '../../../../shared/pipes/relative-time.pipe';
import { presentMovementStatus } from '../../../../shared/strategies/movement-status.presentation';
import { StatusPresentation } from '../../../../shared/strategies/status-presentation.model';
import { presentVehicleStatus } from '../../../../shared/strategies/vehicle-status.presentation';
import { StatusBadgeComponent } from '../../../../shared/ui/status-badge/status-badge.component';

/** Ficha con la información completa del vehículo seleccionado en el buscador. */
@Component({
  selector: 'app-vehicle-detail',
  standalone: true,
  imports: [MatIconModule, StatusBadgeComponent, RelativeTimePipe],
  templateUrl: './vehicle-detail.component.html',
  styleUrl: './vehicle-detail.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class VehicleDetailComponent {
  @Input({ required: true }) vehicle!: Vehicle;

  /** Mismo criterio que la fila del listado: ACTIVE prioriza el estado de movimiento. */
  get presentation(): StatusPresentation {
    return this.vehicle.status === 'ACTIVE'
      ? presentMovementStatus(this.vehicle.movementStatus)
      : presentVehicleStatus(this.vehicle.status);
  }

  get tienePosicion(): boolean {
    return hasPosition(this.vehicle);
  }
}
