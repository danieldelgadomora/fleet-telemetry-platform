import { MovementStatus, VehicleStatus } from './movement-status.model';

/** Forma exacta (snake_case) en la que fleet-gateway-service serializa un vehículo. */
export interface VehicleDto {
  plate: string;
  status: string;
  registered_at: string;
  cache_cleared_at: string | null;
  data_purged_at: string | null;
  last_lat: number | null;
  last_lng: number | null;
  last_reported_at: string | null;
  movement_status: string | null;
}

/** Vehículo de la flota, en la forma (camelCase) que consume el resto de la aplicación. */
export interface Vehicle {
  plate: string;
  status: VehicleStatus;
  registeredAt: string;
  cacheClearedAt: string | null;
  dataPurgedAt: string | null;
  lastLat: number | null;
  lastLng: number | null;
  lastReportedAt: string | null;
  movementStatus: MovementStatus | null;
}

/** Traduce el DTO tal como llega del backend (REST o WebSocket) al modelo de la aplicación. */
export function mapVehicleDto(dto: VehicleDto): Vehicle {
  return {
    plate: dto.plate,
    status: dto.status as VehicleStatus,
    registeredAt: dto.registered_at,
    cacheClearedAt: dto.cache_cleared_at,
    dataPurgedAt: dto.data_purged_at,
    lastLat: dto.last_lat,
    lastLng: dto.last_lng,
    lastReportedAt: dto.last_reported_at,
    movementStatus: dto.movement_status as MovementStatus | null,
  };
}

/** Un vehículo tiene coordenadas válidas para dibujarse en el mapa. */
export function hasPosition(vehicle: Vehicle): vehicle is Vehicle & { lastLat: number; lastLng: number } {
  return vehicle.lastLat !== null && vehicle.lastLng !== null;
}
