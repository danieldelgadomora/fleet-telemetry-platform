import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../core/store/trip_store.dart';
import '../../shared/strategies/alert_rule_presentation.dart';
import '../../shared/theme/app_colors.dart';

/// Historial reciente de alertas filtrado por la placa del conductor. Se
/// alimenta de `GET /api/v1/alerts?limit=N` (sin filtro por placa en el
/// servidor) más `pull-to-refresh`, sin STOMP: mantiene el estado en memoria,
/// sin base de datos local.
class AlertsHistoryScreen extends StatefulWidget {
  const AlertsHistoryScreen({super.key});

  @override
  State<AlertsHistoryScreen> createState() => _AlertsHistoryScreenState();
}

class _AlertsHistoryScreenState extends State<AlertsHistoryScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<TripStore>().refreshAlerts();
    });
  }

  @override
  Widget build(BuildContext context) {
    final tripStore = context.watch<TripStore>();
    final alerts = tripStore.alerts;

    return Scaffold(
      appBar: AppBar(title: const Text('Historial de alertas')),
      body: RefreshIndicator(
        onRefresh: tripStore.refreshAlerts,
        child: alerts.isEmpty
            ? ListView(
                children: const [
                  Padding(
                    padding: EdgeInsets.only(top: 120),
                    child: Center(child: Text('Sin alertas para esta placa todavía')),
                  ),
                ],
              )
            : ListView.separated(
                itemCount: alerts.length,
                separatorBuilder: (_, _) => const Divider(height: 1),
                itemBuilder: (context, index) {
                  final alert = alerts[index];
                  return ListTile(
                    leading: Icon(
                      alert.ruleCode == 'PANIC_BUTTON' ? Icons.warning_amber_rounded : Icons.error_outline,
                      color: alert.ruleCode == 'PANIC_BUTTON'
                          ? AppColors.statusAlert
                          : AppColors.statusStopped,
                    ),
                    title: Text(alertRuleLabel(alert.ruleCode)),
                    subtitle: Text(alert.message),
                    trailing: Text(
                      '${alert.raisedAt.toLocal().hour.toString().padLeft(2, '0')}:'
                      '${alert.raisedAt.toLocal().minute.toString().padLeft(2, '0')}',
                    ),
                  );
                },
              ),
      ),
    );
  }
}
