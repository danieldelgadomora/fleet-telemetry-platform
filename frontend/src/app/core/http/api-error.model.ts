/** Forma exacta del cuerpo de error que devuelve `GlobalExceptionHandler` en el backend. */
export interface ApiErrorDto {
  timestamp: string;
  status: number;
  error: string;
}

/** Error de la API ya tipado, con el mensaje en español listo para mostrar en la UI. */
export class ApiError {
  constructor(
    readonly status: number,
    readonly message: string,
    readonly timestamp: string,
  ) {}
}
