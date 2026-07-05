package com.simon.fleet.ingestion.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VehiclePlateTest {

    @Test
    @DisplayName("normaliza la placa a mayúsculas y sin espacios sobrantes")
    void normalizaAMayusculasYRecortaEspacios() {
        VehiclePlate plate = new VehiclePlate(" abc123 ");

        assertThat(plate.value()).isEqualTo("ABC123");
    }

    @Test
    @DisplayName("rechaza una placa nula")
    void rechazaPlacaNula() {
        assertThatThrownBy(() -> new VehiclePlate(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("rechaza una placa vacía o solo con espacios")
    void rechazaPlacaVaciaOEnBlanco() {
        assertThatThrownBy(() -> new VehiclePlate("   ")).isInstanceOf(IllegalArgumentException.class);
    }
}
