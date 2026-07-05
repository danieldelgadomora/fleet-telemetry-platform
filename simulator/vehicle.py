"""Generación de movimiento (rutas de waypoints + jitter GPS) para cada vehículo simulado."""

from __future__ import annotations

import math
import random
from dataclasses import dataclass, field
from enum import StrEnum

from simulator.cli import SimulatorConfig

WAYPOINTS_PER_ROUTE = 5
ARRIVAL_THRESHOLD_DEG = 0.002
JITTER_SIGMA_DEG = 0.0003
MIN_STEP_FRACTION = 0.15
MAX_STEP_FRACTION = 0.30


class MovementState(StrEnum):
    MOVING = "moving"
    STOPPED = "stopped"


@dataclass
class Vehicle:
    plate: str
    state: MovementState
    waypoints: list[tuple[float, float]]
    rng: random.Random
    waypoint_idx: int = 0
    lat: float = 0.0
    lng: float = 0.0
    last_valid_payload: dict | None = field(default=None)

    def advance_position(self) -> None:
        """Calcula la siguiente posición del vehículo según su estado de movimiento.

        Un vehículo detenido nunca cambia de coordenada: es justo lo que necesita
        `alerting-service` para disparar la alerta de vehículo detenido tras el umbral de tiempo
        configurado. Un vehículo en movimiento avanza una fracción aleatoria de la distancia
        restante hacia su siguiente waypoint (saltando al waypoint siguiente al acercarse lo
        suficiente) y le suma ruido gaussiano para imitar la imprecisión real de un receptor GPS.
        """
        if self.state is MovementState.STOPPED:
            return

        target_lat, target_lng = self.waypoints[self.waypoint_idx]
        distance = math.hypot(target_lat - self.lat, target_lng - self.lng)
        if distance < ARRIVAL_THRESHOLD_DEG:
            self.waypoint_idx = (self.waypoint_idx + 1) % len(self.waypoints)
            target_lat, target_lng = self.waypoints[self.waypoint_idx]

        fraction = self.rng.uniform(MIN_STEP_FRACTION, MAX_STEP_FRACTION)
        self.lat += fraction * (target_lat - self.lat) + self.rng.gauss(0.0, JITTER_SIGMA_DEG)
        self.lng += fraction * (target_lng - self.lng) + self.rng.gauss(0.0, JITTER_SIGMA_DEG)


def _random_waypoints(
    rng: random.Random, base_lat: float, base_lng: float, radius_deg: float
) -> list[tuple[float, float]]:
    return [
        (
            base_lat + rng.uniform(-radius_deg, radius_deg),
            base_lng + rng.uniform(-radius_deg, radius_deg),
        )
        for _ in range(WAYPOINTS_PER_ROUTE)
    ]


def build_fleet(config: SimulatorConfig) -> list[Vehicle]:
    """Construye la flota de vehículos simulados a partir de la configuración de CLI.

    Los primeros `config.stopped_vehicles` vehículos se marcan como detenidos desde el arranque,
    para garantizar el escenario de alerta de vehículo detenido en vez de dejarlo librado
    únicamente al azar del caos.
    """
    fleet: list[Vehicle] = []
    for index in range(config.vehicles):
        seed = None if config.seed is None else config.seed + index
        rng = random.Random(seed)
        waypoints = _random_waypoints(rng, config.base_lat, config.base_lng, config.area_radius_deg)
        state = MovementState.STOPPED if index < config.stopped_vehicles else MovementState.MOVING
        start_lat, start_lng = waypoints[0]
        fleet.append(
            Vehicle(
                plate=f"v{index + 1}",
                state=state,
                waypoints=waypoints,
                rng=rng,
                lat=start_lat,
                lng=start_lng,
            )
        )
    return fleet
