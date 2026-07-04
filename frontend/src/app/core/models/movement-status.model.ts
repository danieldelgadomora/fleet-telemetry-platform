/** Último estado de movimiento conocido de un vehículo, calculado por fleet-gateway-service. */
export type MovementStatus = 'EN_MOVIMIENTO' | 'DETENIDO' | 'ALERTA';

/** Ciclo de vida de un vehículo dentro de la Saga de eliminación. */
export type VehicleStatus = 'ACTIVE' | 'PENDING_DELETION' | 'DELETED';
