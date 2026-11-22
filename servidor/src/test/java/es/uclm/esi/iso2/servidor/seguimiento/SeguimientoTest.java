package es.uclm.esi.iso2.servidor.seguimiento;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import es.uclm.esi.iso2.servidor.catalogo.Prioridad;
import es.uclm.esi.iso2.servidor.comun.ReglaNegocioException;

/** CU10 · Invariantes y ciclo de vida del seguimiento. */
class SeguimientoTest {

    private static final LocalDate PRIMERA = LocalDate.of(2026, 12, 1);

    private static DatosSeguimiento datos(int periodicidad, int revisiones) {
        return new DatosSeguimiento("PAC-1", "US-A", "Control", "Evolución", Prioridad.ORDINARIO, periodicidad,
                revisiones, PRIMERA);
    }

    @ParameterizedTest(name = "periodicidad válida: {0} meses")
    @ValueSource(ints = { 1, 6, 60 })
    void periodicidadValida(int meses) {
        assertEquals(meses, new Seguimiento(1, "medico1", datos(meses, 3)).getPeriodicidadMeses());
    }

    @ParameterizedTest(name = "periodicidad no válida: {0} meses")
    @ValueSource(ints = { -1, 0, 61 })
    void periodicidadNoValida(int meses) {
        assertThrows(IllegalArgumentException.class, () -> new Seguimiento(1, "medico1", datos(meses, 3)));
    }

    @ParameterizedTest(name = "número de revisiones no válido: {0}")
    @ValueSource(ints = { 0, 21 })
    void revisionesNoValidas(int revisiones) {
        assertThrows(IllegalArgumentException.class, () -> new Seguimiento(1, "medico1", datos(6, revisiones)));
    }

    @Test
    @DisplayName("Datos obligatorios: indicación vacía, prioridad o primera revisión nulas")
    void obligatorios() {
        assertThrows(IllegalArgumentException.class, () -> new Seguimiento(1, "medico1",
                new DatosSeguimiento("PAC-1", "US-A", " ", "J", Prioridad.ORDINARIO, 6, 3, PRIMERA)));
        assertThrows(IllegalArgumentException.class, () -> new Seguimiento(1, "medico1",
                new DatosSeguimiento("PAC-1", "US-A", "I", "J", null, 6, 3, PRIMERA)));
        assertThrows(IllegalArgumentException.class, () -> new Seguimiento(1, "medico1",
                new DatosSeguimiento("PAC-1", "US-A", "I", "J", Prioridad.ORDINARIO, 6, 3, null)));
        assertThrows(IllegalArgumentException.class, () -> new Seguimiento(1, "medico1", null));
    }

    @Test
    @DisplayName("Valor límite: la revisión vence el mismo día de la fecha prevista, no antes")
    void vencimiento() {
        Seguimiento s = new Seguimiento(1, "medico1", datos(6, 3));
        assertFalse(s.tieneRevisionVencida(PRIMERA.minusDays(1)));
        assertTrue(s.tieneRevisionVencida(PRIMERA));
        assertTrue(s.tieneRevisionVencida(PRIMERA.plusDays(1)));
    }

    @Test
    @DisplayName("Cada revisión adelanta la próxima fecha; con la última el seguimiento FINALIZA")
    void cicloCompleto() {
        Seguimiento s = new Seguimiento(1, "medico1", datos(6, 2));
        s.registrarRevision(10);
        assertEquals(PRIMERA.plusMonths(6), s.proximaRevision().orElseThrow());
        assertTrue(s.datosRevision().justificacion().contains("revisión 2/2"));
        s.registrarRevision(11);
        assertEquals(EstadoSeguimiento.FINALIZADO, s.getEstado());
        assertTrue(s.proximaRevision().isEmpty());
        assertFalse(s.tieneRevisionVencida(PRIMERA.plusYears(5)));
        assertEquals(2, s.getSolicitudes().size());
        assertThrows(ReglaNegocioException.class, () -> s.registrarRevision(12));
        assertThrows(ReglaNegocioException.class, () -> s.cancelar("tarde"));
    }

    @Test
    @DisplayName("Cancelar exige motivo; un seguimiento cancelado no genera más revisiones")
    void cancelar() {
        Seguimiento s = new Seguimiento(1, "medico1", datos(6, 3));
        assertThrows(IllegalArgumentException.class, () -> s.cancelar(""));
        s.cancelar("Alta del proceso");
        assertEquals(EstadoSeguimiento.CANCELADO, s.getEstado());
        assertEquals("Alta del proceso", s.getMotivoCancelacion());
        assertTrue(s.toString().contains("CANCELADO"));
        assertThrows(ReglaNegocioException.class, () -> s.cancelar("otra vez"));
    }
}
