import { ChangeDetectionStrategy, Component, effect, inject } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatSnackBar } from '@angular/material/snack-bar';
import { FleetStoreService } from '../../../core/store/fleet-store.service';
import { RelativeTimePipe } from '../../../shared/pipes/relative-time.pipe';

/**
 * Panel con las alertas más recientes de la flota. Además, muestra un toast la primera vez que
 * ve cada alerta nueva, para que una alerta no pase desapercibida si el operador no tiene la
 * vista puesta en este panel.
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

  readonly alerts = this.fleetStore.alerts;

  constructor() {
    effect(() => {
      const [masReciente] = this.alerts();
      if (masReciente && masReciente.alertId !== this.ultimaAlertaNotificada) {
        this.ultimaAlertaNotificada = masReciente.alertId;
        this.snackBar.open(`${masReciente.vehicleId}: ${masReciente.message}`, 'Cerrar', { duration: 4000 });
      }
    });
  }
}
