import { Component } from '@angular/core';
import { FleetDashboardPage } from './features/fleet-dashboard/fleet-dashboard.page';

/** Shell raíz de la aplicación: monta la única pantalla del dashboard. */
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [FleetDashboardPage],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
})
export class AppComponent {}
