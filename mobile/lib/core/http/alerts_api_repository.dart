import 'dart:convert';

import 'package:http/http.dart' as http;

import '../config/app_config.dart';
import '../models/alert.dart';

/// Consulta el historial reciente de alertas de toda la flota en
/// `fleet-gateway-service`. El endpoint no filtra por placa: ese filtro se
/// aplica del lado del cliente.
class AlertsApiRepository {
  /// Consulta las `limit` alertas más recientes de toda la flota (sin filtrar por placa).
  Future<List<Alert>> fetchRecent({int limit = AppConfig.alertsHistoryLimit}) async {
    final response = await http.get(
      Uri.parse('${AppConfig.fleetGatewayBaseUrl}/api/v1/alerts?limit=$limit'),
    );
    if (response.statusCode != 200) {
      throw Exception('No se pudo cargar el historial de alertas (HTTP ${response.statusCode})');
    }
    final List<dynamic> body = jsonDecode(response.body) as List<dynamic>;
    return body.map((json) => Alert.fromJson(json as Map<String, dynamic>)).toList();
  }
}
