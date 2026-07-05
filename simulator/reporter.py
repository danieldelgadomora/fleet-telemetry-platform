"""Logging por request y resumen agregado al finalizar el simulador."""

from __future__ import annotations

import logging
import sys
from collections import Counter
from dataclasses import dataclass, field
from datetime import datetime, timezone

from simulator.cli import SimulatorConfig
from simulator.client import SendResult

logger = logging.getLogger("simulator")


def configure_logging(log_level: str) -> None:
    logging.basicConfig(
        level=log_level,
        format="%(asctime)s %(levelname)-7s %(message)s",
        stream=sys.stdout,
    )


@dataclass
class Stats:
    sent_by_label: Counter = field(default_factory=Counter)
    outcomes: Counter = field(default_factory=Counter)
    started_at: datetime = field(default_factory=lambda: datetime.now(timezone.utc))


def _outcome_key(result: SendResult) -> str:
    return "connection_error" if result.error is not None else str(result.status_code)


def log_request(plate: str, label: str, result: SendResult, stats: Stats) -> None:
    """Registra un request en el log de consola y acumula sus contadores para el resumen final."""
    stats.sent_by_label[label] += 1
    stats.outcomes[_outcome_key(result)] += 1

    if result.error is not None:
        logger.error("[%s] chaos=%-22s -> error de conexión: %s", plate, label, result.error)
        return

    note = "  [duplicado intencional — verificar en Mongo, no por status]" if label == "duplicate" else ""
    level = logging.INFO if result.status_code < 300 else logging.WARNING
    logger.log(
        level,
        "[%s] chaos=%-22s -> %s (%.0fms)%s",
        plate,
        label,
        result.status_code,
        result.elapsed_ms,
        note,
    )


def print_summary(stats: Stats, config: SimulatorConfig) -> None:
    """Imprime el resumen agregado de la corrida: totales por tipo de request y por respuesta HTTP."""
    duration = (datetime.now(timezone.utc) - stats.started_at).total_seconds()
    total = sum(stats.sent_by_label.values())

    print("\n" + "=" * 60)
    print("RESUMEN DE LA SIMULACIÓN")
    print("=" * 60)
    print(f"Duración total:      {duration:.1f}s")
    print(f"Vehículos:           {config.vehicles} (detenidos: {config.stopped_vehicles})")
    print(f"Requests enviados:   {total}")

    print("\nPor tipo de request:")
    for label, count in sorted(stats.sent_by_label.items()):
        pct = (count / total * 100) if total else 0.0
        print(f"  {label:<24} {count:>6}  ({pct:5.1f}%)")

    print("\nPor código de respuesta:")
    for outcome, count in sorted(stats.outcomes.items()):
        print(f"  {outcome:<24} {count:>6}")

    print(
        "\nNota: los duplicados intencionales siempre responden 202 (ignorado de forma "
        "idempotente); la única forma de confirmar que el dedupe funcionó de verdad es "
        "comparar contra db.telemetry_history en Mongo."
    )
    print("=" * 60)
