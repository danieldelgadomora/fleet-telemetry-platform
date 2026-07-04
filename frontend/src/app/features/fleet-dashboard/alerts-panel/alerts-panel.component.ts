import { ChangeDetectionStrategy, Component, computed, effect, inject } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatSnackBar } from '@angular/material/snack-bar';
import { FleetStoreService } from '../../../core/store/fleet-store.service';
import { RelativeTimePipe } from '../../../shared/pipes/relative-time.pipe';

/**
 * Panel con las alertas de la flota. Si hay un vehículo seleccionado en el buscador, el panel
 * se filtra a solo sus alertas (vacío si no tiene ninguna); sin selección, muestra las alertas
 * recientes de toda la flota. Además, muestra un toast la primera vez que ve cada alerta nueva
 * de cualquier vehículo, para que no pase desapercibida aunque el panel esté filtrado a otro.
 */
@Component({
  selector: 'app-alerts-panel',
  standalone: true,
  imports: [MatListModule, MatIconModule, RelativeTimePipe],
  templateUrl: './alerts-panel.component.html',
  styleUrl: './alerts-panel.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AlertsPanelComponent {
  private readonly fleetStore = inject(FleetStoreService);
  private readonly snackBar = inject(MatSnackBar);
  private ultimaAlertaNotificada: string | null = null;

  /** Vehículo cuyas alertas se están mostrando en el panel, o `null` si se muestran todas. */
  readonly vehicleFiltrado = this.fleetStore.selectedVehicle;

  readonly alerts = computed(() => {
    const vehicle = this.vehicleFiltrado();
    const alerts = this.fleetStore.alerts();
    return vehicle ? alerts.filter((a) => a.vehicleId === vehicle.vehicleId) : alerts;
  });

  constructor() {
    effect(() => {
      const [masReciente] = this.fleetStore.alerts();
      if (masReciente && masReciente.alertId !== this.ultimaAlertaNotificada) {
        this.ultimaAlertaNotificada = masReciente.alertId;
        this.snackBar.open(`${masReciente.vehicleId}: ${masReciente.message}`, 'Cerrar', { duration: 4000 });
      }
    });
  }
}
