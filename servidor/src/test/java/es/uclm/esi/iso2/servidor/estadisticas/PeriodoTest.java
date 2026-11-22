package es.uclm.esi.iso2.servidor.estadisticas;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** CU20–CU22 · Periodo de una estadística (valores límite). */
class PeriodoTest {

    private static final LocalDate DIA = LocalDate.of(2026, 11, 2);

    @Test
    @DisplayName("Ambos extremos están incluidos; el día anterior y el posterior, no")
    void extremos() {
        Periodo p = new Periodo(DIA, DIA.plusDays(6));
        assertTrue(p.contiene(DIA));
        assertTrue(p.contiene(DIA.plusDays(6).atTime(23, 59)));
        assertFalse(p.contiene(DIA.minusDays(1)));
        assertFalse(p.contiene(DIA.plusDays(7).atStartOfDay()));
        assertFalse(p.contiene((LocalDate) null));
    }

    @Test
    @DisplayName("Periodo de un solo día válido; hasta < desde o fechas nulas, no")
    void validacion() {
        assertTrue(new Periodo(DIA, DIA).contiene(DIA));
        assertThrows(IllegalArgumentException.class, () -> new Periodo(DIA, DIA.minusDays(1)));
        assertThrows(IllegalArgumentException.class, () -> new Periodo(null, DIA));
        assertThrows(IllegalArgumentException.class, () -> new Periodo(DIA, null));
    }
}
