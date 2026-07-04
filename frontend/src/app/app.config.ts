import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { environment } from '../environments/environment';
import { APP_CONFIG } from './core/config/app-config.token';
import { apiErrorInterceptor } from './core/http/api-error.interceptor';
import { REALTIME_CLIENT } from './core/realtime/realtime-client.token';
import { StompClientAdapter } from './core/realtime/stomp-client.adapter';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideAnimationsAsync(),
    provideHttpClient(withInterceptors([apiErrorInterceptor])),
    { provide: APP_CONFIG, useValue: environment },
    { provide: REALTIME_CLIENT, useClass: StompClientAdapter },
  ],
};
