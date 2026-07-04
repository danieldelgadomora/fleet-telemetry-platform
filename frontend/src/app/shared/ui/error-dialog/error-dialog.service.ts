import { inject, Injectable } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ErrorDialogComponent } from './error-dialog.component';

/** Punto único para mostrar errores de la API en un diálogo, nunca en un toast de éxito. */
@Injectable({ providedIn: 'root' })
export class ErrorDialogService {
  private readonly dialog = inject(MatDialog);

  show(message: string): void {
    this.dialog.open(ErrorDialogComponent, { data: message, width: '360px' });
  }
}
