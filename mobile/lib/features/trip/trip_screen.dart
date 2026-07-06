import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../core/store/trip_store.dart';
import '../../shared/theme/app_colors.dart';
import '../alerts/alerts_history_screen.dart';
import '../onboarding/plate_onboarding_screen.dart';
import 'widgets/connection_toggle.dart';
import 'widgets/current_position_tile.dart';
import 'widgets/panic_button.dart';

/// Pantalla principal del conductor: control de viaje, botón de pánico y
/// acceso al historial de alertas. Sin viaje activo no se envía telemetría.
class TripScreen extends StatelessWidget {
  const TripScreen({super.key});

  Future<void> _confirmChangeVehicle(BuildContext context) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (dialogContext) => AlertDialog(
        title: const Text('Cambiar de vehículo'),
        content: const Text(
          '¿Seguro que deseas cambiar de vehículo? Si hay un viaje en curso, se terminará.',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(dialogContext).pop(false),
            child: const Text('Cancelar'),
          ),
          FilledButton(
            onPressed: () => Navigator.of(dialogContext).pop(true),
            child: const Text('Cambiar'),
          ),
        ],
      ),
    );
    if (confirmed != true || !context.mounted) return;

    await context.read<TripStore>().changeVehicle();
    if (!context.mounted) return;

    Navigator.of(context).pushAndRemoveUntil(
      MaterialPageRoute(builder: (_) => const PlateOnboardingScreen()),
      (route) => false,
    );
  }

  @override
  Widget build(BuildContext context) {
    final tripStore = context.watch<TripStore>();

    return Scaffold(
      appBar: AppBar(
        title: Text('Vehículo ${tripStore.plate ?? ''}'),
        actions: [
          IconButton(
            icon: const Icon(Icons.logout),
            tooltip: 'Cambiar de vehículo',
            onPressed: () => _confirmChangeVehicle(context),
          ),
          IconButton(
            icon: const Icon(Icons.notifications_outlined),
            tooltip: 'Historial de alertas',
            onPressed: () => Navigator.of(context).push(
              MaterialPageRoute(builder: (_) => const AlertsHistoryScreen()),
            ),
          ),
        ],
      ),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            const ConnectionToggle(),
            const SizedBox(height: 16),
            const CurrentPositionTile(),
            const SizedBox(height: 16),
            FilledButton.icon(
              onPressed: () =>
                  tripStore.tripActive ? tripStore.endTrip() : tripStore.startTrip(),
              icon: Icon(tripStore.tripActive ? Icons.stop_circle : Icons.play_circle),
              style: FilledButton.styleFrom(
                backgroundColor:
                    tripStore.tripActive ? AppColors.statusAlert : AppColors.statusMoving,
                padding: const EdgeInsets.symmetric(vertical: 16),
              ),
              label: Text(tripStore.tripActive ? 'Terminar viaje' : 'Iniciar viaje'),
            ),
            const Spacer(),
            const PanicButton(),
          ],
        ),
      ),
    );
  }
}
