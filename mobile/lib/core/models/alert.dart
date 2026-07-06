/// Alerta de la flota tal como la expone `GET /api/v1/alerts` en
/// `fleet-gateway-service` (`AlertResponseDto`): claves snake_case y sin
/// filtro por placa — ese filtro se aplica del lado del cliente.
class Alert {
  final String alertId;
  final String plate;
  final String ruleCode;
  final String message;
  final DateTime raisedAt;

  const Alert({
    required this.alertId,
    required this.plate,
    required this.ruleCode,
    required this.message,
    required this.raisedAt,
  });

  factory Alert.fromJson(Map<String, dynamic> json) => Alert(
        alertId: json['alert_id'] as String,
        plate: json['plate'] as String,
        ruleCode: json['rule_code'] as String,
        message: json['message'] as String,
        raisedAt: DateTime.parse(json['raised_at'] as String),
      );
}
