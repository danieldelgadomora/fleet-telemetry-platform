import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

/** Tarjeta pequeña con un conteo y su ícono, usada en el encabezado para resumir la flota. */
@Component({
  selector: 'app-stat-tile',
  standalone: true,
  imports: [MatIconModule],
  templateUrl: './stat-tile.component.html',
  styleUrl: './stat-tile.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StatTileComponent {
  @Input({ required: true }) value!: number;
  @Input({ required: true }) label!: string;
  @Input({ required: true }) icon!: string;
  /** Clase de color semántico opcional (ej. `status-moving`), para relacionar el ícono con el estado que cuenta. */
  @Input() cssClass?: string;
}
