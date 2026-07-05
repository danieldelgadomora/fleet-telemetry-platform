/** Forma exacta (snake_case) en la que fleet-gateway-service representa una alerta, tanto en `/topic/alerts` como en `GET /api/v1/alerts`. */
export interface AlertDto {
  alert_id: string;
  plate: string;
  rule_code: string;
  message: string;
  raised_at: string;
}

/** Alerta de flota, en la forma (camelCase) que consume el resto de la aplicación. */
export interface Alert {
  alertId: string;
  plate: string;
  ruleCode: string;
  message: string;
  raisedAt: string;
}

/** Traduce el DTO (de `/topic/alerts` o de `GET /api/v1/alerts`) al modelo de la aplicación. */
export function mapAlertDto(dto: AlertDto): Alert {
  return {
    alertId: dto.alert_id,
    plate: dto.plate,
    ruleCode: dto.rule_code,
    message: dto.message,
    raisedAt: dto.raised_at,
  };
}
