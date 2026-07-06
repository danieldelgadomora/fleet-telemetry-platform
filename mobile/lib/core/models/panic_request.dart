/// Activación del botón de pánico, con el mismo contrato que espera
/// `POST /api/v1/panic` en `ingestion-service` (`PanicRequestDto`): `lat`,
/// `lng` y `message` son opcionales porque la app puede no tener todavía una
/// posición conocida cuando el conductor presiona el botón.
class PanicRequest {
  final String plate;
  final double? lat;
  final double? lng;
  final String? message;
  final DateTime timestamp;

  const PanicRequest({
    required this.plate,
    this.lat,
    this.lng,
    this.message,
    required this.timestamp,
  });

  /// Serializa a JSON snake_case, tal como espera `PanicRequestDto` en el backend.
  Map<String, dynamic> toJson() => {
        'plate': plate,
        'lat': lat,
        'lng': lng,
        'message': message,
        'timestamp': timestamp.toUtc().toIso8601String(),
      };
}
