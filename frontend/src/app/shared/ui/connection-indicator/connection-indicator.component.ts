import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { ConnectionStatus } from '../../../core/realtime/realtime-client.port';

const LABELS: Record<ConnectionStatus, string> = {
  conectando: 'Conectando…',
  conectado: 'Conectado',
  desconectado: 'Sin conexión',
};

/** Indicador puntual del estado del canal en tiempo real (WebSocket/STOMP) hacia el backend. */
@Component({
  selector: 'app-connection-indicator',
  standalone: true,
  templateUrl: './connection-indicator.component.html',
  styleUrl: './connection-indicator.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ConnectionIndicatorComponent {
  @Input({ required: true }) status!: ConnectionStatus;

  get label(): string {
    return LABELS[this.status];
  }
}
