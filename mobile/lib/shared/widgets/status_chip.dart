import 'package:flutter/material.dart';

/// Chip de estado reutilizable: ícono + etiqueta + color, nunca solo color,
/// igual que las `strategies` de presentación del dashboard Angular no
/// dependen únicamente de un tono para comunicar un estado.
class StatusChip extends StatelessWidget {
  final IconData icon;
  final String label;
  final Color color;

  const StatusChip({super.key, required this.icon, required this.label, required this.color});

  @override
  Widget build(BuildContext context) {
    return Chip(
      avatar: Icon(icon, color: color, size: 18),
      label: Text(label),
      backgroundColor: color.withValues(alpha: 0.12),
      side: BorderSide(color: color.withValues(alpha: 0.4)),
    );
  }
}
