import 'package:path/path.dart';
import 'package:sqflite/sqflite.dart';

/// Abre (y crea si no existe) la base de datos SQLite local que respalda la cola de
/// telemetría pendiente de envío, para que sobreviva a que la app se cierre por completo.
class TelemetryLocalDatabase {
  static Database? _db;

  static Future<Database> get instance async {
    if (_db != null) return _db!;
    final dbPath = await getDatabasesPath();
    _db = await openDatabase(
      join(dbPath, 'telemetry_outbox.db'),
      version: 1,
      onCreate: (db, version) => db.execute('''
        CREATE TABLE pending_telemetry(
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          plate TEXT NOT NULL,
          lat REAL NOT NULL,
          lng REAL NOT NULL,
          timestamp TEXT NOT NULL
        )
      '''),
    );
    return _db!;
  }
}
