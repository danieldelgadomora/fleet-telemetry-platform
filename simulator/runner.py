"""Orquestación asyncio: crea la flota y corre una task por vehículo hasta el corte configurado."""

from __future__ import annotations

import asyncio

import httpx

from simulator import chaos, client, reporter
from simulator.cli import SimulatorConfig
from simulator.reporter import Stats
from simulator.vehicle import Vehicle, build_fleet


async def _vehicle_loop(
    vehicle: Vehicle,
    http_client: httpx.AsyncClient,
    config: SimulatorConfig,
    rates: chaos.ChaosRates,
    stats: Stats,
    stop_event: asyncio.Event,
) -> None:
    """Ciclo de vida de un vehículo: espera un stagger inicial y luego envía una lectura por
    `tick_seconds` hasta que se agoten sus `--iterations` o se dispare `stop_event`."""
    await asyncio.sleep(vehicle.rng.uniform(0, config.tick_seconds))

    iterations_done = 0
    while not stop_event.is_set():
        if config.iterations is not None and iterations_done >= config.iterations:
            return

        chaos_type = chaos.pick_chaos_type(rates, vehicle.rng)
        if chaos_type is chaos.ChaosType.DUPLICATE and vehicle.last_valid_payload is None:
            chaos_type = chaos.ChaosType.VALID
        if chaos_type is not chaos.ChaosType.DUPLICATE:
            vehicle.advance_position()

        request = chaos.build_request(vehicle, chaos_type, vehicle.rng)
        result = await client.send(http_client, config.ingestion_url, request)
        reporter.log_request(vehicle.plate, request.label, result, stats)

        iterations_done += 1
        try:
            await asyncio.wait_for(stop_event.wait(), timeout=config.tick_seconds)
        except asyncio.TimeoutError:
            pass


async def _stop_after(duration_seconds: float, stop_event: asyncio.Event) -> None:
    await asyncio.sleep(duration_seconds)
    stop_event.set()


async def run(config: SimulatorConfig, stats: Stats) -> None:
    """Corre la flota simulada hasta que se agote `--duration-seconds`/`--iterations`, o hasta Ctrl+C."""
    fleet = build_fleet(config)
    rates = chaos.ChaosRates(
        duplicate=config.duplicate_rate,
        invalid_coord=config.invalid_coord_rate,
        bad_timestamp=config.bad_timestamp_rate,
        malformed=config.malformed_rate,
    )
    stop_event = asyncio.Event()

    async with httpx.AsyncClient(timeout=config.timeout_seconds) as http_client:
        tasks = [
            asyncio.create_task(_vehicle_loop(vehicle, http_client, config, rates, stats, stop_event))
            for vehicle in fleet
        ]
        if config.duration_seconds is not None:
            tasks.append(asyncio.create_task(_stop_after(config.duration_seconds, stop_event)))

        await asyncio.gather(*tasks, return_exceptions=True)
