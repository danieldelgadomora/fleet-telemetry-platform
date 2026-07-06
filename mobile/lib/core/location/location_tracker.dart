import 'dart:async';

import 'package:geolocator/geolocator.dart';

import '../config/app_config.dart';

/// Adapta el stream de posiciones de `geolocator`, combinando distancia
/// mínima recorrida y un intervalo mínimo de tiempo entre reportes: evita que
/// cada movimiento pequeño del GPS dispare un envío, que es el criterio de
/// consumo de batería ya documentado en la Propuesta Arquitectónica del
/// README (reporte cada ~15s o por distancia, no cada segundo).
class LocationTracker {
  StreamSubscription<Position>? _subscription;
  DateTime? _lastSentAt;

  Future<void> _ensurePermission() async {
    var permission = await Geolocator.checkPermission();
    if (permission == LocationPermission.denied) {
      permission = await Geolocator.requestPermission();
    }
    if (permission == LocationPermission.denied ||
        permission == LocationPermission.deniedForever) {
      throw Exception('Permiso de ubicación denegado');
    }
  }

  /// Pide permiso si hace falta y empieza a escuchar posiciones, entregando una muestra solo
  /// cuando ya pasó el intervalo mínimo de reporte desde la última entregada.
  Future<void> start(void Function(Position position) onSample) async {
    await _ensurePermission();
    final settings = LocationSettings(
      accuracy: LocationAccuracy.best,
      distanceFilter: AppConfig.reportingDistanceFilterMeters.toInt(),
    );
    _subscription = Geolocator.getPositionStream(locationSettings: settings).listen((position) {
      final now = DateTime.now();
      if (_lastSentAt == null || now.difference(_lastSentAt!) >= AppConfig.reportingInterval) {
        _lastSentAt = now;
        onSample(position);
      }
    });
  }

  /// Cancela la suscripción activa y limpia el throttle, para que un próximo `start` arranque limpio.
  void stop() {
    _subscription?.cancel();
    _subscription = null;
    _lastSentAt = null;
  }
}
