/// Traduce el código estable de una regla de alerta (`rule_code`) a una etiqueta
/// legible en español para el conductor, análogo a `movement-status.presentation.ts`
/// en el dashboard Angular: agregar una regla nueva solo extiende este mapa, sin
/// tocar la pantalla que lo consume.
String alertRuleLabel(String ruleCode) {
  switch (ruleCode) {
    case 'PANIC_BUTTON':
      return 'Botón de pánico';
    case 'STOPPED_VEHICLE':
      return 'Vehículo detenido';
    default:
      return ruleCode;
  }
}
