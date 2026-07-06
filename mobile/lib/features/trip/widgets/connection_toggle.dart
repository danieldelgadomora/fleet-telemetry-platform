import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../../core/store/trip_store.dart';
import '../../../shared/theme/app_colors.dart';
import '../../../shared/widgets/status_chip.dart';

/// Indicador/toggle de "conexión simulada": al apagarlo, las lecturas GPS se
/// encolan localmente en vez de intentar enviarse (atajo manual para probar la
/// cola offline sin depender de cortar la red real del emulador). Cualquier
/// envío que falle de verdad también se encola, con el toggle en cualquier
/// estado — ver `TripStore._onPositionSample`.
class ConnectionToggle extends StatelessWidget {
  const ConnectionToggle({super.key});

  @override
  Widget build(BuildContext context) {
    final tripStore = context.watch<TripStore>();
    final online = tripStore.connectionSimulatedOnline;
    final pendingCount = tripStore.pendingCount;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.center,
      children: [
        InkWell(
          onTap: tripStore.toggleConnectionSimulated,
          borderRadius: BorderRadius.circular(20),
          child: StatusChip(
            icon: online ? Icons.wifi : Icons.wifi_off,
            label: online ? 'Conexión simulada: en línea' : 'Conexión simulada: sin señal',
            color: online ? AppColors.statusMoving : AppColors.statusStopped,
          ),
        ),
        if (pendingCount > 0) ...[
          const SizedBox(height: 8),
          Text(
            '$pendingCount lectura${pendingCount == 1 ? '' : 's'} pendiente${pendingCount == 1 ? '' : 's'} de enviar',
            style: TextStyle(color: AppColors.statusStopped, fontSize: 13),
          ),
        ],
      ],
    );
  }
}
