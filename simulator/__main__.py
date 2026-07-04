"""Punto de entrada: `python -m simulator [flags]` desde la raíz del repositorio."""

from __future__ import annotations

import asyncio
import logging

from simulator import cli, reporter, runner


def main() -> None:
    config = cli.parse_args()
    reporter.configure_logging(config.log_level)
    stats = reporter.Stats()
    try:
        asyncio.run(runner.run(config, stats))
    except KeyboardInterrupt:
        logging.getLogger("simulator").warning("Interrumpido por el usuario (Ctrl+C)")
    finally:
        reporter.print_summary(stats, config)


if __name__ == "__main__":
    main()
