"""Parseo y validación de los argumentos de línea de comandos del simulador."""

from __future__ import annotations

import argparse
from dataclasses import dataclass


@dataclass(frozen=True)
class SimulatorConfig:
    ingestion_url: str
    vehicles: int
    stopped_vehicles: int
    base_lat: float
    base_lng: float
    area_radius_deg: float
    tick_seconds: float
    duplicate_rate: float
    invalid_coord_rate: float
    bad_timestamp_rate: float
    malformed_rate: float
    duration_seconds: float | None
    iterations: int | None
    timeout_seconds: float
    seed: int | None
    log_level: str


def _build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        prog="simulator",
        description=(
            "Simulador de telemetría GPS con inyección de caos para "
            "fleet-telemetry-platform (ingestion-service)."
        ),
    )
    parser.add_argument(
        "--ingestion-url",
        default="http://localhost:8081/api/v1/telemetry",
        help="URL del endpoint de telemetría de ingestion-service.",
    )
    parser.add_argument("--vehicles", type=int, default=5, help="Cantidad de vehículos en la flota.")
    parser.add_argument(
        "--stopped-vehicles",
        type=int,
        default=1,
        help="Cantidad de vehículos (los primeros N) que permanecen detenidos.",
    )
    parser.add_argument("--base-lat", type=float, default=4.6, help="Latitud central del área de simulación.")
    parser.add_argument("--base-lng", type=float, default=-74.08, help="Longitud central del área de simulación.")
    parser.add_argument(
        "--area-radius-deg",
        type=float,
        default=0.05,
        help="Radio (en grados) del bounding box donde se generan los waypoints.",
    )
    parser.add_argument(
        "--tick-seconds",
        type=float,
        default=5.0,
        help="Intervalo en segundos entre envíos consecutivos de un mismo vehículo.",
    )
    parser.add_argument("--duplicate-rate", type=float, default=0.10, help="Probabilidad de reenviar un duplicado exacto.")
    parser.add_argument(
        "--invalid-coord-rate",
        type=float,
        default=0.05,
        help="Probabilidad de enviar una coordenada fuera de rango.",
    )
    parser.add_argument(
        "--bad-timestamp-rate",
        type=float,
        default=0.05,
        help="Probabilidad de enviar un timestamp fuera de la ventana válida.",
    )
    parser.add_argument(
        "--malformed-rate",
        type=float,
        default=0.05,
        help="Probabilidad de enviar un payload malformado (campo faltante, tipo incorrecto o JSON roto).",
    )
    parser.add_argument(
        "--duration-seconds",
        type=float,
        default=None,
        help="Si se indica, el simulador se detiene solo tras esta cantidad de segundos.",
    )
    parser.add_argument(
        "--iterations",
        type=int,
        default=None,
        help="Si se indica, cada vehículo envía como máximo esta cantidad de lecturas.",
    )
    parser.add_argument(
        "--timeout-seconds",
        type=float,
        default=5.0,
        help="Timeout de red por request HTTP.",
    )
    parser.add_argument(
        "--seed",
        type=int,
        default=None,
        help="Semilla para hacer reproducible el run (combinado con --iterations).",
    )
    parser.add_argument(
        "--log-level",
        default="INFO",
        choices=["DEBUG", "INFO", "WARNING", "ERROR"],
        help="Nivel de logging de consola.",
    )
    return parser


def parse_args(argv: list[str] | None = None) -> SimulatorConfig:
    """Parsea `argv` y valida las reglas cruzadas entre flags antes de construir la configuración.

    Se detiene con `parser.error(...)` (código de salida 2) si alguna tasa de caos está fuera de
    `[0, 1]`, si la suma de las cuatro tasas supera 1.0, si la cantidad de vehículos detenidos no
    cabe dentro de la flota, o si la flota está vacía — todo antes de arrancar el bucle `asyncio`.
    """
    parser = _build_parser()
    args = parser.parse_args(argv)

    rates = {
        "--duplicate-rate": args.duplicate_rate,
        "--invalid-coord-rate": args.invalid_coord_rate,
        "--bad-timestamp-rate": args.bad_timestamp_rate,
        "--malformed-rate": args.malformed_rate,
    }
    for name, value in rates.items():
        if not 0.0 <= value <= 1.0:
            parser.error(f"{name} debe estar entre 0 y 1 (recibido: {value})")

    total_rate = sum(rates.values())
    if total_rate > 1.0:
        parser.error(f"la suma de las tasas de caos no puede superar 1.0 (recibido: {total_rate})")

    if args.vehicles < 1:
        parser.error("--vehicles debe ser al menos 1")

    if not 1 <= args.stopped_vehicles <= args.vehicles:
        parser.error("--stopped-vehicles debe estar entre 1 y --vehicles")

    return SimulatorConfig(
        ingestion_url=args.ingestion_url,
        vehicles=args.vehicles,
        stopped_vehicles=args.stopped_vehicles,
        base_lat=args.base_lat,
        base_lng=args.base_lng,
        area_radius_deg=args.area_radius_deg,
        tick_seconds=args.tick_seconds,
        duplicate_rate=args.duplicate_rate,
        invalid_coord_rate=args.invalid_coord_rate,
        bad_timestamp_rate=args.bad_timestamp_rate,
        malformed_rate=args.malformed_rate,
        duration_seconds=args.duration_seconds,
        iterations=args.iterations,
        timeout_seconds=args.timeout_seconds,
        seed=args.seed,
        log_level=args.log_level,
    )
