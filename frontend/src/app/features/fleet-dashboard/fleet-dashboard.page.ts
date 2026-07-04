import { ChangeDetectionStrategy, Component, inject, OnInit } from '@angular/core';
import { FleetStoreService } from '../../core/store/fleet-store.service';
import { AlertsPanelComponent } from './alerts-panel/alerts-panel.component';
import { DashboardHeaderComponent } from './header/dashboard-header.component';
import { FleetMapComponent } from './map/fleet-map.component';
import { VehicleSearchComponent } from './vehicle-list/vehicle-search.component';

/** Página única del dashboard: encabezado, mapa en vivo y panel lateral (vehículos + alertas). */
@Component({
  selector: 'app-fleet-dashboard-page',
  standalone: true,
  imports: [DashboardHeaderComponent, FleetMapComponent, VehicleSearchComponent, AlertsPanelComponent],
  templateUrl: './fleet-dashboard.page.html',
  styleUrl: './fleet-dashboard.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FleetDashboardPage implements OnInit {
  private readonly fleetStore = inject(FleetStoreService);

  /** Arranca la carga inicial y la conexión en tiempo real al montar el dashboard. */
  ngOnInit(): void {
    this.fleetStore.init();
  }
}
