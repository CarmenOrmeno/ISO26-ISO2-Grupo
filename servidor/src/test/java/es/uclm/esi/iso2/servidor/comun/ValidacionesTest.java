package es.uclm.esi.iso2.servidor.comun;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class ValidacionesTest {

    @Test
    @DisplayName("Un texto válido se devuelve sin espacios a los lados")
    void textoValido() {
        assertEquals("hola", Validaciones.texto("  hola ", "campo"));
    }

    @ParameterizedTest(name = "texto inválido «{0}»")
    @NullAndEmptySource
    @ValueSource(strings = { "   ", "\t" })
    void textoInvalido(String valor) {
        assertThrows(IllegalArgumentException.class, () -> Validaciones.texto(valor, "campo"));
    }

    @Test
    void obligatorio() {
        assertEquals(3, Validaciones.obligatorio(3, "n"));
        assertThrows(IllegalArgumentException.class, () -> Validaciones.obligatorio(null, "n"));
    }

    @ParameterizedTest(name = "{0} en [1,5] → válido={1}")
    @CsvSource({ "0,false", "1,true", "3,true", "5,true", "6,false" })
    @DisplayName("Valores límite del rango cerrado [1,5]")
    void rango(int valor, boolean valido) {
        if (valido) {
            assertEquals(valor, Validaciones.rango(valor, 1, 5, "n"));
        } else {
            assertThrows(IllegalArgumentException.class, () -> Validaciones.rango(valor, 1, 5, "n"));
        }
    }
}
