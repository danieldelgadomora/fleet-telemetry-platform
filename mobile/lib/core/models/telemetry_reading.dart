/// Lectura GPS de un vehículo, con el mismo contrato snake_case que espera
/// `POST /api/v1/telemetry` en `ingestion-service` (`TelemetryRequestDto`).
class TelemetryReading {
  final String plate;
  final double lat;
  final double lng;
  final DateTime timestamp;

  const TelemetryReading({
    required this.plate,
    required this.lat,
    required this.lng,
    required this.timestamp,
  });

  Map<String, dynamic> toJson() => {
        'plate': plate,
        'lat': lat,
        'lng': lng,
        'timestamp': timestamp.toUtc().toIso8601String(),
      };
}
