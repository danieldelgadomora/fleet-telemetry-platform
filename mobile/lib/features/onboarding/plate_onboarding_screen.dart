import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../core/store/trip_store.dart';
import '../trip/trip_screen.dart';

/// Pantalla de ingreso de placa: se muestra una única vez (o hasta que se
/// borren los datos de la app), ya que la placa queda persistida con
/// `shared_preferences` y se reutiliza en cada request al backend.
class PlateOnboardingScreen extends StatefulWidget {
  const PlateOnboardingScreen({super.key});

  @override
  State<PlateOnboardingScreen> createState() => _PlateOnboardingScreenState();
}

class _PlateOnboardingScreenState extends State<PlateOnboardingScreen> {
  final _controller = TextEditingController();
  final _formKey = GlobalKey<FormState>();

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  Future<void> _continue() async {
    if (!_formKey.currentState!.validate()) return;
    final tripStore = context.read<TripStore>();
    await tripStore.setPlate(_controller.text);
    if (!mounted) return;
    Navigator.of(context).pushReplacement(
      MaterialPageRoute(builder: (_) => const TripScreen()),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Form(
            key: _formKey,
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                const Icon(Icons.local_shipping, size: 64),
                const SizedBox(height: 16),
                const Text(
                  'Ingresa la placa de tu vehículo',
                  style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 24),
                TextFormField(
                  controller: _controller,
                  textCapitalization: TextCapitalization.characters,
                  decoration: const InputDecoration(
                    labelText: 'Placa',
                    hintText: 'ABC123',
                    border: OutlineInputBorder(),
                  ),
                  validator: (value) =>
                      (value == null || value.trim().isEmpty) ? 'La placa es obligatoria' : null,
                  onFieldSubmitted: (_) => _continue(),
                ),
                const SizedBox(height: 24),
                SizedBox(
                  width: double.infinity,
                  child: FilledButton(
                    onPressed: _continue,
                    child: const Padding(
                      padding: EdgeInsets.symmetric(vertical: 12),
                      child: Text('Continuar'),
                    ),
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
