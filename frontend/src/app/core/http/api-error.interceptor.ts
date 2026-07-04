import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';
import { ApiError, ApiErrorDto } from './api-error.model';

function isApiErrorDto(body: unknown): body is ApiErrorDto {
  return typeof body === 'object' && body !== null && 'error' in body && 'status' in body;
}

/**
 * Decora cada respuesta de error HTTP traduciendo el cuerpo `{timestamp, status, error}` que
 * ya devuelve `GlobalExceptionHandler` del backend a un {@link ApiError} tipado, sin que cada
 * llamada individual tenga que repetir esa lógica de parseo.
 */
export const apiErrorInterceptor: HttpInterceptorFn = (request, next) =>
  next(request).pipe(
    catchError((response: HttpErrorResponse) => {
      const body: unknown = response.error;
      const apiError = isApiErrorDto(body)
        ? new ApiError(body.status, body.error, body.timestamp)
        : new ApiError(response.status, 'No se pudo completar la operación. Intenta de nuevo.', new Date().toISOString());
      return throwError(() => apiError);
    }),
  );
