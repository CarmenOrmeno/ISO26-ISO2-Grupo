package es.uclm.esi.iso2.servidor.catalogo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TipoPruebaTest {

    static TipoPrueba conDuracion(int minutos) {
        return new TipoPrueba("RX", "Rx tórax", Modalidad.RX, "RX1", minutos, null, false, false, null, false);
    }

    @ParameterizedTest(name = "duración {0} min → válida={1}")
    @CsvSource({ "4,false", "5,true", "120,true", "240,true", "241,false" })
    @DisplayName("Valores límite de la duración [5, 240]")
    void limitesDuracion(int minutos, boolean valida) {
        if (valida) {
            assertEquals(minutos, conDuracion(minutos).duracionMinutos());
        } else {
            assertThrows(IllegalArgumentException.class, () -> conDuracion(minutos));
        }
    }

    @ParameterizedTest(name = "contraste={0}, consentimiento={1} → válido={2}")
    @CsvSource({ "false,false,true", "false,true,true", "true,true,true", "true,false,false" })
    @DisplayName("Tabla de decisión contraste × consentimiento")
    void contrasteExigeConsentimiento(boolean contraste, boolean consentimiento, boolean valido) {
        if (valido) {
            new TipoPrueba("TC", "TAC", Modalidad.TC, "TC1", 30, "", contraste, consentimiento, "", false);
        } else {
            assertThrows(IllegalArgumentException.class,
                    () -> new TipoPrueba("TC", "TAC", Modalidad.TC, "TC1", 30, "", contraste, consentimiento, "", false));
        }
    }

    @Test
    @DisplayName("Preparación y requisitos nulos se guardan como texto vacío")
    void opcionales() {
        TipoPrueba t = conDuracion(10);
        assertEquals("", t.preparacion());
        assertEquals("", t.requisitosSeguridad());
        assertFalse(t.necesitaPreparacion());
        TipoPrueba conPreparacion = new TipoPrueba("US", "Eco", Modalidad.US, "E1", 20, " Ayuno ", false, false, "", true);
        assertTrue(conPreparacion.necesitaPreparacion());
        assertEquals("Ayuno", conPreparacion.preparacion());
        assertTrue(conPreparacion.toString().contains("admite seguimiento"));
    }

    @Test
    @DisplayName("Código, nombre, modalidad y sala son obligatorios")
    void obligatorios() {
        assertThrows(IllegalArgumentException.class,
                () -> new TipoPrueba("", "N", Modalidad.RX, "S", 10, "", false, false, "", false));
        assertThrows(IllegalArgumentException.class,
                () -> new TipoPrueba("C", null, Modalidad.RX, "S", 10, "", false, false, "", false));
        assertThrows(IllegalArgumentException.class,
                () -> new TipoPrueba("C", "N", null, "S", 10, "", false, false, "", false));
        assertThrows(IllegalArgumentException.class,
                () -> new TipoPrueba("C", "N", Modalidad.RX, " ", 10, "", false, false, "", false));
    }

    @Test
    void descripcionModalidad() {
        assertEquals("Resonancia magnética", Modalidad.RM.descripcion());
    }
}
