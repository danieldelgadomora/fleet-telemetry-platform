import 'package:flutter_test/flutter_test.dart';
import 'package:provider/provider.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'package:fleet_driver_app/core/store/trip_store.dart';
import 'package:fleet_driver_app/main.dart';

void main() {
  testWidgets('Muestra el onboarding de placa cuando no hay una guardada', (tester) async {
    SharedPreferences.setMockInitialValues({});

    await tester.pumpWidget(
      ChangeNotifierProvider(
        create: (_) => TripStore(),
        child: const FleetDriverApp(),
      ),
    );
    await tester.pumpAndSettle();

    expect(find.text('Ingresa la placa de tu vehículo'), findsOneWidget);
  });
}
