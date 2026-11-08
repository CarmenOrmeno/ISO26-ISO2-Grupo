package es.uclm.esi.iso2.servidor.citas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import es.uclm.esi.iso2.servidor.comun.ReglaNegocioException;

class CitaTest {

    static final LocalDateTime MOMENTO = LocalDateTime.of(2026, 11, 3, 9, 0);

    static Cita nueva() {
        return new Cita(1, 10, "PAC-1", 5, MOMENTO);
    }

    @Test
    @DisplayName("Una cita nueva está PROGRAMADA")
    void citaNueva() {
        Cita c = nueva();
        assertEquals(EstadoCita.PROGRAMADA, c.getEstado());
        assertEquals(10, c.getIdSolicitud());
        assertEquals(5, c.getIdAgenda());
        assertEquals(0, c.getReprogramaciones());
        assertTrue(c.toString().contains("PAC-1"));
    }

    @Test
    @DisplayName("Reprogramar cambia hueco, cuenta la reprogramación y vuelve a PROGRAMADA")
    void reprogramar() {
        Cita c = nueva();
        c.reprogramar(6, MOMENTO.plusDays(1));
        assertEquals(6, c.getIdAgenda());
        assertEquals(MOMENTO.plusDays(1), c.getFechaHora());
        assertEquals(1, c.getReprogramaciones());
        assertThrows(IllegalArgumentException.class, () -> c.reprogramar(6, null));
    }

    @Test
    @DisplayName("Cancelar guarda motivo y momento; una cancelada no se reprograma ni se cancela otra vez")
    void cancelar() {
        Cita c = nueva();
        assertThrows(IllegalArgumentException.class, () -> c.cancelar(" ", MOMENTO));
        c.cancelar("Paciente ingresado", MOMENTO.minusDays(1));
        assertEquals(EstadoCita.CANCELADA, c.getEstado());
        assertEquals("Paciente ingresado", c.getMotivoCancelacion());
        assertEquals(MOMENTO.minusDays(1), c.getFechaCancelacion());
        assertThrows(ReglaNegocioException.class, () -> c.cancelar("otra", MOMENTO));
        assertThrows(ReglaNegocioException.class, () -> c.reprogramar(6, MOMENTO));
    }

    @ParameterizedTest(name = "{0} activa")
    @EnumSource(value = EstadoCita.class, names = { "PROGRAMADA", "CONFIRMADA", "CAMBIO_SOLICITADO" })
    void activas(EstadoCita e) {
        assertTrue(e.activa());
    }

    @ParameterizedTest(name = "{0} no activa")
    @EnumSource(value = EstadoCita.class, names = { "CANCELADA", "REALIZADA" })
    void inactivas(EstadoCita e) {
        assertFalse(e.activa());
    }

    @Test
    void obligatorios() {
        assertThrows(IllegalArgumentException.class, () -> new Cita(1, 1, "", 1, MOMENTO));
        assertThrows(IllegalArgumentException.class, () -> new Cita(1, 1, "PAC-1", 1, null));
    }

    @Test
    @DisplayName("CU07: confirmar solo desde PROGRAMADA; reprogramar obliga a confirmar de nuevo")
    void confirmar() {
        Cita c = nueva();
        c.confirmar();
        assertEquals(EstadoCita.CONFIRMADA, c.getEstado());
        assertThrows(ReglaNegocioException.class, c::confirmar);
        c.reprogramar(6, MOMENTO.plusDays(1));
        assertEquals(EstadoCita.PROGRAMADA, c.getEstado());
        c.confirmar();
    }

    @Test
    @DisplayName("CU07: solicitar cambio desde PROGRAMADA o CONFIRMADA, no dos veces ni tras cancelar")
    void solicitarCambio() {
        Cita c = nueva();
        c.confirmar();
        assertThrows(IllegalArgumentException.class, () -> c.solicitarCambio(""));
        c.solicitarCambio("Viaje de trabajo");
        assertEquals(EstadoCita.CAMBIO_SOLICITADO, c.getEstado());
        assertEquals("Viaje de trabajo", c.getMotivoCambio());
        assertThrows(ReglaNegocioException.class, () -> c.solicitarCambio("otra vez"));
        assertThrows(ReglaNegocioException.class, c::confirmar);
        Cita cancelada = nueva();
        cancelada.cancelar("x", MOMENTO);
        assertThrows(ReglaNegocioException.class, () -> cancelada.solicitarCambio("y"));
    }
}
