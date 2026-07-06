import 'package:flutter/foundation.dart';
import 'package:geolocator/geolocator.dart';

import '../config/app_config.dart';
import '../http/alerts_api_repository.dart';
import '../http/telemetry_api_repository.dart';
import '../location/location_tracker.dart';
import '../models/alert.dart';
import '../models/panic_request.dart';
import '../models/telemetry_reading.dart';
import '../storage/driver_prefs_repository.dart';
import '../storage/telemetry_outbox_repository.dart';

/// Facade que combina persistencia de placa, seguimiento de ubicación y
/// acceso HTTP detrás de un único `ChangeNotifier`: ninguna pantalla llama
/// `http`, `geolocator` ni `shared_preferences` directamente, igual que
/// `FleetStoreService` en el frontend Angular combina Repository + Adapter
/// de tiempo real detrás de Signals de solo lectura.
class TripStore extends ChangeNotifier {
  TripStore({
    DriverPrefsRepository? driverPrefsRepository,
    TelemetryApiRepository? telemetryApiRepository,
    AlertsApiRepository? alertsApiRepository,
    LocationTracker? locationTracker,
    TelemetryOutboxRepository? telemetryOutboxRepository,
  })  : _driverPrefsRepository = driverPrefsRepository ?? DriverPrefsRepository(),
        _telemetryApiRepository = telemetryApiRepository ?? TelemetryApiRepository(),
        _alertsApiRepository = alertsApiRepository ?? AlertsApiRepository(),
        _locationTracker = locationTracker ?? LocationTracker(),
        _telemetryOutboxRepository = telemetryOutboxRepository ?? TelemetryOutboxRepository();

  final DriverPrefsRepository _driverPrefsRepository;
  final TelemetryApiRepository _telemetryApiRepository;
  final AlertsApiRepository _alertsApiRepository;
  final LocationTracker _locationTracker;
  final TelemetryOutboxRepository _telemetryOutboxRepository;

  String? _plate;
  bool _tripActive = false;
  bool _connectionSimulatedOnline = true;
  Position? _lastPosition;
  List<Alert> _alerts = [];
  String? _lastError;
  int _pendingCount = 0;

  String? get plate => _plate;
  bool get tripActive => _tripActive;
  bool get connectionSimulatedOnline => _connectionSimulatedOnline;
  Position? get lastPosition => _lastPosition;
  List<Alert> get alerts => List.unmodifiable(_alerts);
  String? get lastError => _lastError;
  int get pendingCount => _pendingCount;

  /// Carga la placa persistida (si hay una) y el número de lecturas pendientes de la cola,
  /// para que la pantalla inicial sepa si debe saltar directo al viaje o pedir el onboarding.
  Future<void> loadPlate() async {
    _plate = await _driverPrefsRepository.getPlate();
    _pendingCount = await _telemetryOutboxRepository.count();
    notifyListeners();
  }

  /// Persiste la placa ingresada por el conductor y la deja disponible para el resto de la app.
  Future<void> setPlate(String plate) async {
    await _driverPrefsRepository.savePlate(plate);
    _plate = plate.trim().toUpperCase();
    notifyListeners();
  }

  /// Alterna el estado de "conexión simulada". Al pasar a "en línea" dispara un intento de
  /// vaciar la cola de lecturas pendientes; al pasar a "sin señal", las siguientes lecturas se
  /// encolan en vez de intentar enviarse.
  void toggleConnectionSimulated() {
    _connectionSimulatedOnline = !_connectionSimulatedOnline;
    notifyListeners();
    if (_connectionSimulatedOnline) _flushPendingQueue();
  }

  /// Arranca el reporte periódico de posición del viaje activo; sin placa o con un viaje ya
  /// activo, no hace nada.
  Future<void> startTrip() async {
    if (_tripActive || _plate == null) return;
    _lastError = null;
    await _locationTracker.start(_onPositionSample);
    _tripActive = true;
    notifyListeners();
  }

  /// Detiene el reporte periódico de posición: sin viaje activo no se envía telemetría.
  void endTrip() {
    if (!_tripActive) return;
    _locationTracker.stop();
    _tripActive = false;
    notifyListeners();
  }

  /// Cierra el viaje activo (si lo hay), descarta el estado del vehículo actual
  /// y borra la placa persistida, para que el conductor pueda ingresar una nueva.
  Future<void> changeVehicle() async {
    endTrip();
    _lastPosition = null;
    _alerts = [];
    _lastError = null;
    await _driverPrefsRepository.clearPlate();
    _plate = null;
    notifyListeners();
  }

  void _onPositionSample(Position position) {
    _lastPosition = position;
    notifyListeners();
    final plate = _plate;
    if (plate == null) return;
    final reading = TelemetryReading(
      plate: plate,
      lat: position.latitude,
      lng: position.longitude,
      timestamp: DateTime.now(),
    );

    if (!_connectionSimulatedOnline) {
      // Conexión simulada apagada: se encola directamente, sin intentar la red.
      _enqueueAndRefreshCount(reading);
      return;
    }
    _telemetryApiRepository.postTelemetry(reading).then((_) {
      debugPrint('[TripStore] POST /api/v1/telemetry OK -> $plate ${reading.lat},${reading.lng}');
      // Cada envío exitoso funciona como "latido" para intentar vaciar pendientes
      // acumuladas por fallas reales de red anteriores.
      _flushPendingQueue();
    }).catchError((error) {
      debugPrint('[TripStore] POST /api/v1/telemetry FALLÓ -> $error');
      _enqueueAndRefreshCount(reading);
    });
  }

  Future<void> _enqueueAndRefreshCount(TelemetryReading reading) async {
    await _telemetryOutboxRepository.enqueue(reading);
    _pendingCount = await _telemetryOutboxRepository.count();
    notifyListeners();
  }

  /// Reenvía las lecturas pendientes en orden cronológico de captura, en lotes
  /// pequeños. Se detiene en el primer fallo (deja el resto en cola para el
  /// próximo latido) para no reordenar ni saltarse lecturas.
  Future<void> _flushPendingQueue() async {
    final batch = await _telemetryOutboxRepository.peekBatch(AppConfig.outboxFlushBatchSize);
    for (final entry in batch) {
      try {
        await _telemetryApiRepository.postTelemetry(entry.value);
        await _telemetryOutboxRepository.remove(entry.key);
        _pendingCount = await _telemetryOutboxRepository.count();
        notifyListeners();
      } catch (_) {
        break;
      }
    }
  }

  /// Activa el botón de pánico con la última posición conocida (o sin ella, si aún no hay
  /// ninguna) y una nota opcional del conductor.
  Future<void> triggerPanic({String? message}) async {
    final plate = _plate;
    if (plate == null) return;
    try {
      await _telemetryApiRepository.postPanic(PanicRequest(
        plate: plate,
        lat: _lastPosition?.latitude,
        lng: _lastPosition?.longitude,
        message: message,
        timestamp: DateTime.now(),
      ));
      _lastError = null;
    } catch (e) {
      _lastError = e.toString();
    }
    notifyListeners();
  }

  /// Recarga el historial de alertas de toda la flota y lo filtra a las de la placa actual.
  Future<void> refreshAlerts() async {
    final plate = _plate;
    if (plate == null) return;
    try {
      final allAlerts = await _alertsApiRepository.fetchRecent();
      _alerts = allAlerts.where((alert) => alert.plate == plate).toList();
      _lastError = null;
    } catch (e) {
      _lastError = e.toString();
    }
    notifyListeners();
  }

  @override
  void dispose() {
    _locationTracker.stop();
    super.dispose();
  }
}
