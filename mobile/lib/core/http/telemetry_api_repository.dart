import 'dart:convert';

import 'package:http/http.dart' as http;

import '../config/app_config.dart';
import '../models/panic_request.dart';
import '../models/telemetry_reading.dart';

/// Envía telemetría y activaciones del botón de pánico a `ingestion-service`.
class TelemetryApiRepository {
  Future<void> postTelemetry(TelemetryReading reading) async {
    await http.post(
      Uri.parse('${AppConfig.ingestionBaseUrl}/api/v1/telemetry'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode(reading.toJson()),
    );
  }

  Future<void> postPanic(PanicRequest request) async {
    final response = await http.post(
      Uri.parse('${AppConfig.ingestionBaseUrl}/api/v1/panic'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode(request.toJson()),
    );
    if (response.statusCode != 202) {
      throw Exception('El botón de pánico no pudo confirmarse (HTTP ${response.statusCode})');
    }
  }
}
