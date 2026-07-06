import 'package:sqflite/sqflite.dart';

import '../config/app_config.dart';
import '../models/telemetry_reading.dart';
import 'telemetry_local_database.dart';

/// Cola local (outbox) de lecturas GPS que no se pudieron enviar de inmediato (sin
/// conexión simulada o falla real de red). Conserva el timestamp de captura original —
/// nunca se re-sella con la hora del reenvío — y respeta un tope de tamaño descartando
/// primero las lecturas más viejas, igual que documenta la Propuesta Arquitectónica del README.
class TelemetryOutboxRepository {
  Future<void> enqueue(TelemetryReading reading) async {
    final db = await TelemetryLocalDatabase.instance;
    final currentCount = Sqflite.firstIntValue(
      await db.rawQuery('SELECT COUNT(*) FROM pending_telemetry'),
    )!;
    if (currentCount >= AppConfig.maxOutboxSize) {
      await db.delete(
        'pending_telemetry',
        where: 'id = (SELECT id FROM pending_telemetry ORDER BY id ASC LIMIT 1)',
      );
    }
    await db.insert('pending_telemetry', {
      'plate': reading.plate,
      'lat': reading.lat,
      'lng': reading.lng,
      'timestamp': reading.timestamp.toUtc().toIso8601String(),
    });
  }

  /// Devuelve hasta [limit] lecturas pendientes, en orden cronológico de captura
  /// (las más antiguas primero), junto con su id local para poder borrarlas al confirmarse.
  Future<List<MapEntry<int, TelemetryReading>>> peekBatch(int limit) async {
    final db = await TelemetryLocalDatabase.instance;
    final rows = await db.query('pending_telemetry', orderBy: 'id ASC', limit: limit);
    return rows
        .map((row) => MapEntry(
              row['id'] as int,
              TelemetryReading(
                plate: row['plate'] as String,
                lat: row['lat'] as double,
                lng: row['lng'] as double,
                timestamp: DateTime.parse(row['timestamp'] as String),
              ),
            ))
        .toList();
  }

  Future<void> remove(int id) async {
    final db = await TelemetryLocalDatabase.instance;
    await db.delete('pending_telemetry', where: 'id = ?', whereArgs: [id]);
  }

  Future<int> count() async {
    final db = await TelemetryLocalDatabase.instance;
    return Sqflite.firstIntValue(await db.rawQuery('SELECT COUNT(*) FROM pending_telemetry')) ?? 0;
  }
}
