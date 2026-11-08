package es.uclm.esi.iso2.servidor.catalogo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ParametrosSistemaTest {

    private final ParametrosSistema p = new ParametrosSistema();

    @Test
    @DisplayName("Valores por defecto")
    void porDefecto() {
        assertEquals(LocalTime.of(8, 0), p.apertura());
        assertEquals(LocalTime.of(21, 0), p.cierre());
        assertEquals(1, p.plazoMaximoDias(Prioridad.URGENTE));
        assertEquals(15, p.plazoMaximoDias(Prioridad.PREFERENTE));
        assertEquals(90, p.plazoMaximoDias(Prioridad.ORDINARIO));
        assertEquals(24, p.horasAntelacionRecordatorio());
        assertTrue(p.toString().contains("08:00-21:00"));
    }

    @ParameterizedTest(name = "horario {0}-{1} → válido={2}")
    @CsvSource({ "09:00,14:00,true", "14:00,14:00,false", "15:00,14:00,false" })
    void cambiarHorario(LocalTime apertura, LocalTime cierre, boolean valido) {
        if (valido) {
            p.cambiarHorario(apertura, cierre);
            assertEquals(apertura, p.apertura());
        } else {
            assertThrows(IllegalArgumentException.class, () -> p.cambiarHorario(apertura, cierre));
        }
    }

    @Test
    void horarioNulo() {
        assertThrows(IllegalArgumentException.class, () -> p.cambiarHorario(null, LocalTime.NOON));
        assertThrows(IllegalArgumentException.class, () -> p.cambiarHorario(LocalTime.NOON, null));
    }

    @ParameterizedTest(name = "{0} = {1} días → válido={2}")
    @CsvSource({ "URGENTE,0,false", "URGENTE,1,true", "URGENTE,15,true", "URGENTE,16,false",
            "PREFERENTE,1,true", "PREFERENTE,90,true", "PREFERENTE,91,false",
            "ORDINARIO,15,true", "ORDINARIO,14,false", "ORDINARIO,365,true", "ORDINARIO,366,false" })
    @DisplayName("Plazos: rango [1, 365] y orden URGENTE ≤ PREFERENTE ≤ ORDINARIO")
    void cambiarPlazo(Prioridad prioridad, int dias, boolean valido) {
        if (valido) {
            p.cambiarPlazo(prioridad, dias);
            assertEquals(dias, p.plazoMaximoDias(prioridad));
        } else {
            assertThrows(IllegalArgumentException.class, () -> p.cambiarPlazo(prioridad, dias));
        }
    }

    @ParameterizedTest(name = "recordatorio {0} h → válido={1}")
    @CsvSource({ "0,false", "1,true", "168,true", "169,false" })
    void horasRecordatorio(int horas, boolean valido) {
        if (valido) {
            p.cambiarHorasRecordatorio(horas);
            assertEquals(horas, p.horasAntelacionRecordatorio());
        } else {
            assertThrows(IllegalArgumentException.class, () -> p.cambiarHorasRecordatorio(horas));
        }
    }

    @ParameterizedTest(name = "[{0}, {1}] dentro de 08:00-21:00 → {2}")
    @CsvSource({ "07:59,08:30,false", "08:00,08:30,true", "20:30,21:00,true", "20:30,21:01,false",
            "10:00,10:00,false" })
    @DisplayName("Valores límite del horario del servicio")
    void dentroDeHorario(LocalTime inicio, LocalTime fin, boolean dentro) {
        assertEquals(dentro, p.dentroDeHorario(inicio, fin));
    }

    @Test
    void plazoNulo() {
        assertThrows(IllegalArgumentException.class, () -> p.cambiarPlazo(null, 3));
        assertFalse(p.dentroDeHorario(LocalTime.of(22, 0), LocalTime.of(23, 0)));
    }
}
