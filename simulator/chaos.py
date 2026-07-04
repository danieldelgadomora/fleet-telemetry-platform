"""Inyección de caos: sorteo del tipo de resultado por request y construcción del payload."""

from __future__ import annotations

import copy
import json
import random
from dataclasses import dataclass
from datetime import datetime, timedelta, timezone
from enum import StrEnum

from simulator.vehicle import Vehicle

INVALID_LAT_CHOICES = (91.5, -95.2, 132.7)
INVALID_LNG_CHOICES = (185.0, -200.4, 271.9)
FUTURE_SKEW_OVER = timedelta(minutes=5)
PAST_AGE_OVER = timedelta(hours=30)


class ChaosType(StrEnum):
    VALID = "valid"
    DUPLICATE = "duplicate"
    INVALID_COORD = "invalid_coord"
    BAD_TIMESTAMP = "bad_timestamp"
    MALFORMED = "malformed"


@dataclass(frozen=True)
class ChaosRates:
    duplicate: float
    invalid_coord: float
    bad_timestamp: float
    malformed: float

    def ordered(self) -> list[tuple[ChaosType, float]]:
        return [
            (ChaosType.DUPLICATE, self.duplicate),
            (ChaosType.INVALID_COORD, self.invalid_coord),
            (ChaosType.BAD_TIMESTAMP, self.bad_timestamp),
            (ChaosType.MALFORMED, self.malformed),
        ]


@dataclass(frozen=True)
class SimulatedRequest:
    chaos_type: ChaosType
    label: str
    json_body: dict | None
    raw_body: bytes | None
    headers: dict[str, str]


def pick_chaos_type(rates: ChaosRates, rng: random.Random) -> ChaosType:
    """Sortea el tipo de resultado de un request según las tasas configuradas por CLI.

    Se sortea un único tipo por request (nunca una combinación de varios) para que cada tasa
    siga siendo, en aislamiento, la probabilidad real de ese resultado, y para que cada línea de
    log tenga una sola causa inequívoca.
    """
    roll = rng.random()
    cumulative = 0.0
    for chaos_type, rate in rates.ordered():
        cumulative += rate
        if roll < cumulative:
            return chaos_type
    return ChaosType.VALID


def _format_timestamp(moment: datetime) -> str:
    return moment.isoformat().replace("+00:00", "Z")


def build_valid_body(vehicle: Vehicle) -> dict:
    return {
        "vehicle_id": vehicle.vehicle_id,
        "lat": vehicle.lat,
        "lng": vehicle.lng,
        "timestamp": _format_timestamp(datetime.now(timezone.utc)),
    }


def build_invalid_coord_body(valid_body: dict, rng: random.Random) -> dict:
    """Reemplaza `lat` o `lng` por un valor fuera de rango, ejercitando el
    `IllegalArgumentException` que lanza `Coordinates` en el backend."""
    body = dict(valid_body)
    if rng.random() < 0.5:
        body["lat"] = rng.choice(INVALID_LAT_CHOICES)
    else:
        body["lng"] = rng.choice(INVALID_LNG_CHOICES)
    return body


def build_bad_timestamp_body(valid_body: dict, rng: random.Random) -> dict:
    """Genera un timestamp fuera de la ventana `[now-24h, now+1min]` de
    `ValidTimestampSpecification`, con margen suficiente para no depender de la latencia real
    de red en el límite exacto de la ventana."""
    now = datetime.now(timezone.utc)
    bad_timestamp = now + FUTURE_SKEW_OVER if rng.random() < 0.5 else now - PAST_AGE_OVER
    body = dict(valid_body)
    body["timestamp"] = _format_timestamp(bad_timestamp)
    return body


def build_malformed_request(valid_body: dict, rng: random.Random) -> tuple[str, dict | None, bytes | None]:
    """Construye una de tres variantes de payload verdaderamente malformado.

    Devuelve la subetiqueta (para el log/resumen) y, exclusivamente uno de los dos, el cuerpo
    como dict o el cuerpo crudo en bytes.
    """
    variant = rng.choice(["missing_field", "wrong_type", "broken_json"])

    if variant == "missing_field":
        body = dict(valid_body)
        del body[rng.choice(list(body.keys()))]
        return variant, body, None

    if variant == "wrong_type":
        body = dict(valid_body)
        broken_field = rng.choice(["lat", "lng"])
        body[broken_field] = f"invalid_{broken_field}"
        return variant, body, None

    # broken_json: corrompe el texto serializado a propósito, ya no es deserializable a un dict.
    text = json.dumps(valid_body).rstrip("}") + ","
    return variant, None, text.encode("utf-8")


def build_request(vehicle: Vehicle, chaos_type: ChaosType, rng: random.Random) -> SimulatedRequest:
    """Construye el request a enviar para el tipo de caos ya sorteado.

    El caso `DUPLICATE` reenvía literalmente el último payload válido del vehículo (mismo
    `lat`/`lng`/`timestamp` exactos) en vez de generar una posición nueva, porque el dedupe real
    de `ingestion-service` compara los `double` crudos de la coordenada — una coordenada
    "parecida" no produce un duplicado real.
    """
    if chaos_type is ChaosType.DUPLICATE:
        return SimulatedRequest(
            chaos_type=ChaosType.DUPLICATE,
            label="duplicate",
            json_body=copy.deepcopy(vehicle.last_valid_payload),
            raw_body=None,
            headers={},
        )

    valid_body = build_valid_body(vehicle)
    vehicle.last_valid_payload = valid_body

    if chaos_type is ChaosType.INVALID_COORD:
        return SimulatedRequest(
            chaos_type=chaos_type,
            label="invalid_coord",
            json_body=build_invalid_coord_body(valid_body, rng),
            raw_body=None,
            headers={},
        )

    if chaos_type is ChaosType.BAD_TIMESTAMP:
        return SimulatedRequest(
            chaos_type=chaos_type,
            label="bad_timestamp",
            json_body=build_bad_timestamp_body(valid_body, rng),
            raw_body=None,
            headers={},
        )

    if chaos_type is ChaosType.MALFORMED:
        variant, json_body, raw_body = build_malformed_request(valid_body, rng)
        headers = {"Content-Type": "application/json"} if raw_body is not None else {}
        return SimulatedRequest(
            chaos_type=chaos_type,
            label=f"malformed:{variant}",
            json_body=json_body,
            raw_body=raw_body,
            headers=headers,
        )

    return SimulatedRequest(
        chaos_type=ChaosType.VALID,
        label="valid",
        json_body=valid_body,
        raw_body=None,
        headers={},
    )
