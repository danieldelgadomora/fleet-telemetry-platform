import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../../core/store/trip_store.dart';

/// Muestra la última posición GPS real reportada por el dispositivo durante
/// el viaje activo (o un estado vacío si aún no hay ninguna).
class CurrentPositionTile extends StatelessWidget {
  const CurrentPositionTile({super.key});

  @override
  Widget build(BuildContext context) {
    final position = context.watch<TripStore>().lastPosition;
    return Card(
      child: ListTile(
        leading: const Icon(Icons.my_location),
        title: Text(
          position == null
              ? 'Sin posición aún'
              : '${position.latitude.toStringAsFixed(6)}, ${position.longitude.toStringAsFixed(6)}',
        ),
        subtitle: Text(position == null
            ? 'Inicia el viaje para reportar tu ubicación'
            : 'Última posición reportada'),
      ),
    );
  }
}
