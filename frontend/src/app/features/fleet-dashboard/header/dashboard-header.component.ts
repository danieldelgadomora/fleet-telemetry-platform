import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { MatToolbarModule } from '@angular/material/toolbar';
import { FleetStoreService } from '../../../core/store/fleet-store.service';
import { ConnectionIndicatorComponent } from '../../../shared/ui/connection-indicator/connection-indicator.component';
import { StatTileComponent } from './stat-tile/stat-tile.component';

/** Encabezado del dashboard: marca, indicador de conexión y resumen de la flota por estado. */
@Component({
  selector: 'app-dashboard-header',
  standalone: true,
  imports: [MatToolbarModule, ConnectionIndicatorComponent, StatTileComponent],
  templateUrl: './dashboard-header.component.html',
  styleUrl: './dashboard-header.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardHeaderComponent {
  private readonly fleetStore = inject(FleetStoreService);

  readonly connectionStatus = this.fleetStore.connectionStatus;
  readonly stats = this.fleetStore.stats;
}
