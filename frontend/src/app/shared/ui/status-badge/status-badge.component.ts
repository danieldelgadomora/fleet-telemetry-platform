import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { StatusPresentation } from '../../strategies/status-presentation.model';

/** Badge de estado reutilizable: ícono + etiqueta + color semántico, nunca solo color. */
@Component({
  selector: 'app-status-badge',
  standalone: true,
  imports: [MatIconModule],
  templateUrl: './status-badge.component.html',
  styleUrl: './status-badge.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StatusBadgeComponent {
  @Input({ required: true }) presentation!: StatusPresentation;
}
