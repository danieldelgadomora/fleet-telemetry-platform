/** Forma exacta (snake_case) en la que fleet-gateway-service publica una alerta por STOMP. */
export interface AlertDto {
  alert_id: string;
  vehicle_id: string;
  rule_code: string;
  message: string;
  raised_at: string;
}

/** Alerta de flota, en la forma (camelCase) que consume el resto de la aplicación. */
export interface Alert {
  alertId: string;
  vehicleId: string;
  ruleCode: string;
  message: string;
  raisedAt: string;
}

/** Traduce el DTO recibido en `/topic/alerts` al modelo de la aplicación. */
export function mapAlertDto(dto: AlertDto): Alert {
  return {
    alertId: dto.alert_id,
    vehicleId: dto.vehicle_id,
    ruleCode: dto.rule_code,
    message: dto.message,
    raisedAt: dto.raised_at,
  };
}
