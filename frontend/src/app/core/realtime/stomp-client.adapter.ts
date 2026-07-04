import { inject, Injectable, signal } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { APP_CONFIG } from '../config/app-config.token';
import { ConnectionStatus, RealtimeClientPort } from './realtime-client.port';

interface Subscription {
  destination: string;
  onMessage: (payload: unknown) => void;
}

/**
 * Adaptador concreto de {@link RealtimeClientPort} sobre `@stomp/stompjs` + `sockjs-client`. Es
 * la única clase de la aplicación que conoce estas dos librerías.
 */
@Injectable()
export class StompClientAdapter implements RealtimeClientPort {
  private readonly config = inject(APP_CONFIG);
  private readonly pendingSubscriptions: Subscription[] = [];
  private readonly client: Client;

  private readonly _connectionStatus = signal<ConnectionStatus>('desconectado');
  readonly connectionStatus = this._connectionStatus.asReadonly();

  constructor() {
    this.client = new Client({
      webSocketFactory: () => new SockJS(this.config.wsUrl),
      reconnectDelay: 3000,
      onConnect: () => this.onConnected(),
      onWebSocketClose: () => this._connectionStatus.set('desconectado'),
      onStompError: () => this._connectionStatus.set('desconectado'),
    });
  }

  connect(): void {
    this._connectionStatus.set('conectando');
    this.client.activate();
  }

  subscribe<T>(destination: string, onMessage: (payload: T) => void): void {
    const subscription: Subscription = { destination, onMessage: onMessage as (payload: unknown) => void };
    this.pendingSubscriptions.push(subscription);
    if (this.client.connected) {
      this.applySubscription(subscription);
    }
  }

  /**
   * Se ejecuta cada vez que se establece (o restablece) la conexión: reaplica todas las
   * suscripciones pedidas hasta ahora, porque una reconexión abre una sesión STOMP nueva y las
   * suscripciones de la sesión anterior ya no existen en el servidor.
   */
  private onConnected(): void {
    this._connectionStatus.set('conectado');
    this.pendingSubscriptions.forEach((subscription) => this.applySubscription(subscription));
  }

  private applySubscription(subscription: Subscription): void {
    this.client.subscribe(subscription.destination, (message: IMessage) =>
      subscription.onMessage(JSON.parse(message.body)),
    );
  }
}
