package com.simon.fleet.ingestion.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CoordinatesTest {

    @Test
    @DisplayName("acepta coordenadas dentro del rango válido")
    void aceptaCoordenadasEnRangoValido() {
        Coordinates coordinates = new Coordinates(4.6, -74.08);

        assertThat(coordinates.lat()).isEqualTo(4.6);
        assertThat(coordinates.lng()).isEqualTo(-74.08);
    }

    @Test
    @DisplayName("rechaza una latitud fuera de rango")
    void rechazaLatitudFueraDeRango() {
        assertThatThrownBy(() -> new Coordinates(90.1, 0.0)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("rechaza una longitud fuera de rango")
    void rechazaLongitudFueraDeRango() {
        assertThatThrownBy(() -> new Coordinates(0.0, -180.1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("dos coordenadas que difieren solo después del sexto decimal se consideran la misma ubicación")
    void coordenadasQueDifierenDespuesDelSextoDecimalSonLaMismaUbicacion() {
        Coordinates a = new Coordinates(4.600000, -74.080000);
        Coordinates b = new Coordinates(4.6000001, -74.0800001);

        assertThat(a.isSameLocationAs(b)).isTrue();
    }

    @Test
    @DisplayName("dos coordenadas que difieren en el sexto decimal se consideran ubicaciones distintas")
    void coordenadasQueDifierenEnElSextoDecimalSonUbicacionesDistintas() {
        Coordinates a = new Coordinates(4.600000, -74.080000);
        Coordinates b = new Coordinates(4.600002, -74.080000);

        assertThat(a.isSameLocationAs(b)).isFalse();
    }
}
