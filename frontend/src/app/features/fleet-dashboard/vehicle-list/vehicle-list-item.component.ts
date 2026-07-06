import { ChangeDetectionStrategy, Component, inject, Input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ApiError } from '../../../core/http/api-error.model';
import { Vehicle } from '../../../core/models/vehicle.model';
import { FleetStoreService } from '../../../core/store/fleet-store.service';
import { RelativeTimePipe } from '../../../shared/pipes/relative-time.pipe';
import { presentMovementStatus } from '../../../shared/strategies/movement-status.presentation';
import { StatusPresentation } from '../../../shared/strategies/status-presentation.model';
import { presentVehicleStatus } from '../../../shared/strategies/vehicle-status.presentation';
import { ConfirmDialogComponent } from '../../../shared/ui/confirm-dialog/confirm-dialog.component';
import { StatusBadgeComponent } from '../../../shared/ui/status-badge/status-badge.component';

/** Fila de la lista de vehículos: estado, última posición reportada y acción de borrado. */
@Component({
  selector: 'app-vehicle-list-item',
  standalone: true,
  imports: [MatIconModule, MatButtonModule, StatusBadgeComponent, RelativeTimePipe],
  templateUrl: './vehicle-list-item.component.html',
  styleUrl: './vehicle-list-item.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class VehicleListItemComponent {
  @Input({ required: true }) vehicle!: Vehicle;

  private readonly fleetStore = inject(FleetStoreService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  /**
   * Mientras el vehículo está `ACTIVE`, lo relevante para el operador es su estado de
   * movimiento; en cualquier otro punto del ciclo de vida (ej. camino a `DELETED`), se prioriza
   * mostrar ese estado en vez del de movimiento.
   */
  get presentation(): StatusPresentation {
    return this.vehicle.status === 'ACTIVE'
      ? presentMovementStatus(this.vehicle.movementStatus)
      : presentVehicleStatus(this.vehicle.status);
  }

  get puedeEliminarse(): boolean {
    return this.vehicle.status === 'ACTIVE';
  }

  get isSelected(): boolean {
    return this.fleetStore.selectedPlate() === this.vehicle.plate;
  }

  /** Selecciona esta fila (o la deselecciona si ya estaba seleccionada) para verla en el mapa. */
  seleccionar(): void {
    this.fleetStore.selectVehicle(this.isSelected ? null : this.vehicle.plate);
  }

  /** Pide confirmación y, si se acepta, arranca la Saga de eliminación del vehículo. */
  eliminar(): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Eliminar vehículo',
        message: `¿Eliminar el vehículo "${this.vehicle.plate}"? Esta acción no se puede deshacer.`,
        confirmLabel: 'Eliminar',
      },
    });

    dialogRef.afterClosed().subscribe((confirmado: boolean) => {
      if (!confirmado) {
        return;
      }
      this.fleetStore.removeVehicle(this.vehicle.plate).subscribe({
        error: (error: ApiError) =>
          this.snackBar.open(error.message, 'Cerrar', { duration: 4000, panelClass: 'snackbar-error' }),
      });
    });
  }
}
