import { Pipe, PipeTransform } from '@angular/core';

/**
 * Muestra una marca de tiempo ISO-8601 como tiempo relativo en español (ej. "hace 12 s",
 * "hace 3 min"), para que la fecha de última posición/registro se lea de un vistazo.
 */
@Pipe({
  name: 'relativeTime',
  standalone: true,
  pure: false, // el valor debe recalcularse en cada detección de cambios, no solo cuando cambia el input
})
export class RelativeTimePipe implements PipeTransform {
  transform(value: string | null): string {
    if (!value) {
      return 'sin datos';
    }
    const segundos = Math.max(0, Math.floor((Date.now() - new Date(value).getTime()) / 1000));
    if (segundos < 60) {
      return `hace ${segundos} s`;
    }
    const minutos = Math.floor(segundos / 60);
    if (minutos < 60) {
      return `hace ${minutos} min`;
    }
    const horas = Math.floor(minutos / 60);
    if (horas < 24) {
      return `hace ${horas} h`;
    }
    const dias = Math.floor(horas / 24);
    return `hace ${dias} d`;
  }
}
