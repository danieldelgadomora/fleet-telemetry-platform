"""Cliente HTTP fino sobre httpx.AsyncClient para enviar los requests simulados (válidos o caóticos)."""

from __future__ import annotations

import time
from dataclasses import dataclass

import httpx

from simulator.chaos import SimulatedRequest


@dataclass(frozen=True)
class SendResult:
    status_code: int | None
    elapsed_ms: float
    error: str | None


async def send(client: httpx.AsyncClient, url: str, request: SimulatedRequest) -> SendResult:
    """Envía el request simulado y mide el tiempo de respuesta.

    Los errores de red/conexión no se propagan: se devuelven como parte del resultado para que
    el llamador pueda registrar el fallo y seguir con el siguiente tick sin tumbar el simulador
    completo (ej. si el backend está caído).
    """
    start = time.perf_counter()
    try:
        if request.raw_body is not None:
            response = await client.post(url, content=request.raw_body, headers=request.headers)
        else:
            response = await client.post(url, json=request.json_body, headers=request.headers)
        elapsed_ms = (time.perf_counter() - start) * 1000
        return SendResult(status_code=response.status_code, elapsed_ms=elapsed_ms, error=None)
    except httpx.RequestError as exc:
        elapsed_ms = (time.perf_counter() - start) * 1000
        return SendResult(status_code=None, elapsed_ms=elapsed_ms, error=str(exc))
