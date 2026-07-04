import { InjectionToken } from '@angular/core';
import { RealtimeClientPort } from './realtime-client.port';

/** Token de inyección del puerto de tiempo real, para poder sustituir el adaptador concreto. */
export const REALTIME_CLIENT = new InjectionToken<RealtimeClientPort>('REALTIME_CLIENT');
