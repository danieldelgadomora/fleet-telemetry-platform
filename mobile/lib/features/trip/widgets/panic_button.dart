import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../../core/store/trip_store.dart';
import '../../../shared/theme/app_colors.dart';

/// Botón de pánico con confirmación explícita, para evitar activaciones por
/// toques accidentales dado lo crítico de la acción.
class PanicButton extends StatelessWidget {
  const PanicButton({super.key});

  Future<void> _confirmAndTrigger(BuildContext context) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (dialogContext) => AlertDialog(
        title: const Text('Confirmar botón de pánico'),
        content: const Text('¿Seguro que deseas activar el botón de pánico?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(dialogContext).pop(false),
            child: const Text('Cancelar'),
          ),
          FilledButton(
            onPressed: () => Navigator.of(dialogContext).pop(true),
            style: FilledButton.styleFrom(backgroundColor: AppColors.statusAlert),
            child: const Text('Activar'),
          ),
        ],
      ),
    );
    if (confirmed != true || !context.mounted) return;

    final tripStore = context.read<TripStore>();
    await tripStore.triggerPanic();
    if (!context.mounted) return;

    final error = tripStore.lastError;
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(error == null
            ? 'Alerta de pánico enviada'
            : 'No se pudo enviar la alerta: $error'),
        backgroundColor: error == null ? AppColors.statusMoving : AppColors.statusAlert,
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: double.infinity,
      child: FilledButton.icon(
        onPressed: () => _confirmAndTrigger(context),
        style: FilledButton.styleFrom(
          backgroundColor: AppColors.statusAlert,
          padding: const EdgeInsets.symmetric(vertical: 16),
        ),
        icon: const Icon(Icons.warning_amber_rounded),
        label: const Text('BOTÓN DE PÁNICO'),
      ),
    );
  }
}
