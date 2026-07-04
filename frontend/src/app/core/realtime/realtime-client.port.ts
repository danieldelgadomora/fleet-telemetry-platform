import { Signal } from '@angular/core';

/** Estado de la conexión en tiempo real con el backend. */
export type ConnectionStatus = 'conectando' | 'conectado' | 'desconectado';

/**
 * Puerto de salida hacia el canal de tiempo real. Ningún componente ni servicio de la
 * aplicación conoce la librería concreta (STOMP/SockJS) que hay detrás: solo dependen de esta
 * interfaz, implementada por un adaptador (ver {@link StompClientAdapter}).
 */
export interface RealtimeClientPort {
  /** Estado actual de la conexión, para reflejarlo en la UI. */
  readonly connectionStatus: Signal<ConnectionStatus>;

  /** Abre la conexión (con reconexión automática a cargo del adaptador). */
  connect(): void;

  /**
   * Se suscribe a un destino y traduce cada mensaje entrante con `onMessage`. Es seguro
   * llamarla antes de que la conexión esté establecida: el adaptador aplica la suscripción en
   * cuanto conecta (y en cada reconexión).
   */
  subscribe<T>(destination: string, onMessage: (payload: T) => void): void;
}
