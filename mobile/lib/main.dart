import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import 'core/store/trip_store.dart';
import 'features/onboarding/plate_onboarding_screen.dart';
import 'features/trip/trip_screen.dart';
import 'shared/theme/app_colors.dart';

void main() {
  runApp(
    ChangeNotifierProvider(
      create: (_) => TripStore(),
      child: const FleetDriverApp(),
    ),
  );
}

class FleetDriverApp extends StatelessWidget {
  const FleetDriverApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'App del Conductor',
      theme: ThemeData(
        brightness: Brightness.dark,
        colorScheme: ColorScheme.fromSeed(
          seedColor: AppColors.seed,
          brightness: Brightness.dark,
        ),
        useMaterial3: true,
      ),
      home: const StartupScreen(),
    );
  }
}

/// Decide la pantalla inicial según si ya hay una placa persistida en el
/// dispositivo (`shared_preferences`): si existe, salta directo al viaje.
class StartupScreen extends StatefulWidget {
  const StartupScreen({super.key});

  @override
  State<StartupScreen> createState() => _StartupScreenState();
}

class _StartupScreenState extends State<StartupScreen> {
  late final Future<void> _loadPlateFuture;

  @override
  void initState() {
    super.initState();
    _loadPlateFuture = context.read<TripStore>().loadPlate();
  }

  @override
  Widget build(BuildContext context) {
    return FutureBuilder<void>(
      future: _loadPlateFuture,
      builder: (context, snapshot) {
        if (snapshot.connectionState != ConnectionState.done) {
          return const Scaffold(body: Center(child: CircularProgressIndicator()));
        }
        final plate = context.watch<TripStore>().plate;
        return plate == null ? const PlateOnboardingScreen() : const TripScreen();
      },
    );
  }
}
