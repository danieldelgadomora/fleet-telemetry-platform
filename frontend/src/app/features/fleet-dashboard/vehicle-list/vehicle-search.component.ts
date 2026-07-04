import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { FleetStoreService } from '../../../core/store/fleet-store.service';
import { RegisterVehicleFormComponent } from './register-vehicle-form.component';
import { VehicleDetailComponent } from './vehicle-detail/vehicle-detail.component';
import { VehicleListItemComponent } from './vehicle-list-item.component';

/**
 * Sección de búsqueda: filtro por id, listado seleccionable y detalle del vehículo elegido. El
 * alta de un vehículo vive aparte, en un diálogo, para no compartir espacio ni confundirse con
 * el buscador.
 */
@Component({
  selector: 'app-vehicle-search',
  standalone: true,
  imports: [
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatButtonModule,
    VehicleListItemComponent,
    VehicleDetailComponent,
  ],
  templateUrl: './vehicle-search.component.html',
  styleUrl: './vehicle-search.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class VehicleSearchComponent {
  private readonly fleetStore = inject(FleetStoreService);
  private readonly dialog = inject(MatDialog);

  readonly searchTerm = signal('');

  readonly selectedVehicle = this.fleetStore.selectedVehicle;

  /** Vehículos que coinciden con el término de búsqueda (substring de `vehicleId`, sin distinguir mayúsculas). */
  readonly filteredVehicles = computed(() => {
    const term = this.searchTerm().trim().toLowerCase();
    const vehicles = this.fleetStore.vehicles();
    return term ? vehicles.filter((v) => v.vehicleId.toLowerCase().includes(term)) : vehicles;
  });

  /** Abre el diálogo de alta; el listado se actualiza solo vía el signal del store. */
  abrirRegistro(): void {
    this.dialog.open(RegisterVehicleFormComponent, { width: '360px' });
  }
}
