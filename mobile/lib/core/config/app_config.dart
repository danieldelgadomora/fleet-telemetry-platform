/// Configuración de endpoints del backend consumidos por la app del conductor.
///
/// `10.0.2.2` es el alias que el emulador Android expone hacia `localhost` del
/// equipo anfitrión: no apunta a una IP real, solo permite que el emulador
/// alcance los servicios que corren en la máquina donde se ejecuta Docker.
class AppConfig {
  static const String ingestionBaseUrl = 'http://10.0.2.2:8081';
  static const String fleetGatewayBaseUrl = 'http://10.0.2.2:8083';

  static const int alertsHistoryLimit = 50;
  static const Duration reportingInterval = Duration(seconds: 15);
  static const double reportingDistanceFilterMeters = 15;

  static const int maxOutboxSize = 100;
  static const int outboxFlushBatchSize = 20;
}
