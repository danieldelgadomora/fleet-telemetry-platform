import { InjectionToken } from '@angular/core';

/** Configuración de entorno que necesita la aplicación para hablar con el backend. */
export interface AppEnvironment {
  /** URL base de la API REST de fleet-gateway-service (sin barra final). */
  apiBaseUrl: string;
  /** URL del endpoint STOMP/SockJS de fleet-gateway-service. */
  wsUrl: string;
  /** URL base de la API REST de ingestion-service (sin barra final): solo la usa el historial de recorrido para trazar rutas. */
  ingestionBaseUrl: string;
}

/**
 * Token de inyección para la configuración de entorno. Los servicios dependen de este token en
 * vez de importar `environment` directamente, para no acoplarse al mecanismo de reemplazo de
 * archivos de Angular CLI.
 */
export const APP_CONFIG = new InjectionToken<AppEnvironment>('APP_CONFIG');
