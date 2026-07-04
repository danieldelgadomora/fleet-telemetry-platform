import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ApiError } from '../../../core/http/api-error.model';
import { FleetStoreService } from '../../../core/store/fleet-store.service';
import { ErrorDialogService } from '../../../shared/ui/error-dialog/error-dialog.service';

/**
 * Contenido de diálogo para el alta de un vehículo nuevo: es la única forma de registrar, para
 * que la acción quede separada de la búsqueda/listado en vez de compartir espacio con ella. Si
 * el backend la rechaza (409, id ya existe), el error se muestra en un diálogo aparte, nunca en
 * el mismo toast verde que usa el éxito.
 */
@Component({
  selector: 'app-register-vehicle-form',
  standalone: true,
  imports: [ReactiveFormsModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule, MatDialogModule],
  templateUrl: './register-vehicle-form.component.html',
  styleUrl: './register-vehicle-form.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RegisterVehicleFormComponent {
  private readonly fleetStore = inject(FleetStoreService);
  private readonly formBuilder = inject(FormBuilder);
  private readonly snackBar = inject(MatSnackBar);
  private readonly errorDialog = inject(ErrorDialogService);
  private readonly dialogRef = inject(MatDialogRef<RegisterVehicleFormComponent>);

  readonly form = this.formBuilder.nonNullable.group({
    vehicleId: ['', [Validators.required, Validators.pattern(/^\S+$/)]],
  });

  /** Envía el alta; si tiene éxito cierra el diálogo, si el backend la rechaza muestra el error. */
  registrar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const vehicleId = this.form.getRawValue().vehicleId;
    this.fleetStore.registerVehicle(vehicleId).subscribe({
      next: () => {
        this.snackBar.open(`Vehículo ${vehicleId} registrado.`, 'Cerrar', {
          duration: 3000,
          panelClass: 'snackbar-success',
        });
        this.dialogRef.close();
      },
      error: (error: ApiError) => this.errorDialog.show(error.message),
    });
  }

  cancelar(): void {
    this.dialogRef.close();
  }
}
