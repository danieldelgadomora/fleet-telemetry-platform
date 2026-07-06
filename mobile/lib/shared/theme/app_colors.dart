import 'package:flutter/material.dart';

/// Colores de marca y de estado, alineados 1:1 con `frontend/src/styles/theme.scss` y
/// `styles.scss` — misma semilla de marca y los mismos códigos de estado de negocio, para que
/// la app del conductor y el dashboard web se perciban como un solo producto.
class AppColors {
  static const seed = Color(0xFF00C896);

  static const statusMoving = Color(0xFF0CA30C);
  static const statusStopped = Color(0xFFFAB219);
  static const statusAlert = Color(0xFFD03B3B);
  static const statusSerious = Color(0xFFEC835A);
}
