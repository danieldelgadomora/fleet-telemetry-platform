/** Forma exacta (snake_case) en la que ingestion-service serializa un punto de historial. */
export interface TelemetryHistoryPointDto {
  lat: number;
  lng: number;
  recorded_at: string;
}

/** Punto del recorrido de un vehículo, en la forma (camelCase) que consume el resto de la aplicación. */
export interface TelemetryHistoryPoint {
  lat: number;
  lng: number;
  recordedAt: string;
}

/** Traduce el DTO tal como llega de ingestion-service al modelo de la aplicación. */
export function mapTelemetryHistoryPointDto(dto: TelemetryHistoryPointDto): TelemetryHistoryPoint {
  return {
    lat: dto.lat,
    lng: dto.lng,
    recordedAt: dto.recorded_at,
  };
}
