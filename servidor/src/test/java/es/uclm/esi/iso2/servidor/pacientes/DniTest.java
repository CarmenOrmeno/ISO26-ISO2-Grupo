package es.uclm.esi.iso2.servidor.pacientes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class DniTest {

    @ParameterizedTest(name = "«{0}» → {1}")
    @CsvSource({ "12345678Z,12345678Z", "' 12345678z ',12345678Z", "00000000T,00000000T", "99999999R,99999999R" })
    @DisplayName("DNI válidos (incluidos los límites 00000000 y 99999999) se normalizan")
    void validos(String dni, String esperado) {
        assertEquals(esperado, Dni.normalizar(dni));
    }

    @ParameterizedTest(name = "«{0}» inválido")
    @NullSource
    @ValueSource(strings = { "", "1234567Z", "123456789Z", "12345678", "1234567AZ", "12345678A", "12345678Ñ" })
    @DisplayName("DNI con longitud, formato o letra de control incorrectos")
    void invalidos(String dni) {
        assertThrows(IllegalArgumentException.class, () -> Dni.normalizar(dni));
    }
}
