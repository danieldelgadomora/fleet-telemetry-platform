import 'package:shared_preferences/shared_preferences.dart';

/// Persiste la placa del conductor en el dispositivo, para no pedirla de
/// nuevo cada vez que se abre la app.
class DriverPrefsRepository {
  static const _plateKey = 'driver_plate';

  Future<String?> getPlate() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(_plateKey);
  }

  Future<void> savePlate(String plate) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_plateKey, plate.trim().toUpperCase());
  }

  /// Elimina la placa persistida, para permitir que el conductor ingrese una nueva
  /// la próxima vez que abra la app (ej. cambio de vehículo en el turno).
  Future<void> clearPlate() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_plateKey);
  }
}
