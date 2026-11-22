package es.uclm.esi.iso2.servidor.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import es.uclm.esi.iso2.servidor.api.ApiHistorial;
import es.uclm.esi.iso2.servidor.catalogo.Modalidad;
import es.uclm.esi.iso2.servidor.catalogo.Prioridad;
import es.uclm.esi.iso2.servidor.comun.AccesoDenegadoException;
import es.uclm.esi.iso2.servidor.comun.NoEncontradoException;
import es.uclm.esi.iso2.servidor.comun.ReglaNegocioException;
import es.uclm.esi.iso2.servidor.historial.EntradaHistorial;
import es.uclm.esi.iso2.servidor.solicitudes.EstadoSolicitud;

/** It11 · CU13 Consultar el historial de pruebas realizadas. */
class ControladorHistorialTest {

    private static final LocalDate MANANA = Entorno.MANANA;

    private final Entorno e = new Entorno().conDatosBasicos();
    private final ApiHistorial historial = e.servidor.historial();
    private long rx;
    private long eco;
    private long pendiente;

    /** PAC-1: Rx realizada a las 9:10 sin informe, eco realizada a las 9:40 e informada, TAC aún sin realizar. */
    @BeforeEach
    void preparar() {
        rx = e.solicitar("medico1", "PAC-1", "RX-T", Prioridad.ORDINARIO);
        eco = e.solicitar("medico2", "PAC-1", "US-A", Prioridad.ORDINARIO);
        pendiente = e.solicitar("medico1", "PAC-1", "TC-C", Prioridad.ORDINARIO);
        long citaRx = e.citar(rx, e.agenda(Modalidad.RX, "RX1", "medico1"), "09:00");
        long citaEco = e.citar(eco, e.agenda(Modalidad.US, "ECO1", "medico2"), "09:30");
        // la fecha de realización es el momento en que el médico la registra
        e.reloj.fijar(MANANA.atTime(9, 10));
        e.servidor.realizacion().registrarRealizacion(e.token("medico1"), citaRx, 1, "");
        e.reloj.fijar(MANANA.atTime(9, 40));
        e.servidor.realizacion().registrarRealizacion(e.token("medico2"), citaEco, 2, "");
        e.servidor.informes().crearInforme(e.token("medico2"), eco, "Hígado homogéneo", "Sin alteraciones");
    }

    private List<Long> ids(List<EntradaHistorial> entradas) {
        return entradas.stream().map(EntradaHistorial::idSolicitud).toList();
    }

    @Test
    @DisplayName("CU13: solo las pruebas realizadas o informadas, de la más reciente a la más antigua")
    void historial() {
        List<EntradaHistorial> mio = historial.miHistorial(e.token("paciente1"));
        assertEquals(List.of(eco, rx), ids(mio));
        assertFalse(ids(mio).contains(pendiente));
        EntradaHistorial ecoEntrada = mio.get(0);
        assertEquals(EstadoSolicitud.INFORMADA, ecoEntrada.estado());
        assertTrue(ecoEntrada.informeDisponible());
        assertEquals("Eco abdominal", ecoEntrada.nombreTipo());
        assertEquals(Modalidad.US, ecoEntrada.modalidad());
        assertFalse(mio.get(1).informeDisponible());
        assertTrue(mio.get(1).toString().contains("pendiente de informe"));
    }

    @Test
    @DisplayName("CU13: cada paciente ve solo su historial (PAC-2 no tiene pruebas realizadas)")
    void soloElPropio() {
        assertTrue(historial.miHistorial(e.token("paciente2")).isEmpty());
    }

    @Test
    @DisplayName("CU13: valores límite del intervalo de fechas (desde = hasta = día de la prueba)")
    void intervalo() {
        String t = e.token("paciente1");
        assertEquals(2, historial.miHistorial(t, MANANA, MANANA).size());
        assertTrue(historial.miHistorial(t, MANANA.plusDays(1), MANANA.plusDays(30)).isEmpty());
        assertTrue(historial.miHistorial(t, MANANA.minusDays(30), MANANA.minusDays(1)).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> historial.miHistorial(t, MANANA, MANANA.minusDays(1)));
        assertThrows(IllegalArgumentException.class, () -> historial.miHistorial(t, null, MANANA));
    }

    @Test
    @DisplayName("CU13: el paciente lee el informe de su prueba informada")
    void miInforme() {
        assertEquals("Sin alteraciones", historial.miInforme(e.token("paciente1"), eco).conclusion());
    }

    @Test
    @DisplayName("CU13: prueba sin informe o inexistente → NoEncontrado; prueba de otro paciente → AccesoDenegado")
    void informeNoDisponible() {
        assertThrows(NoEncontradoException.class, () -> historial.miInforme(e.token("paciente1"), rx));
        assertThrows(NoEncontradoException.class, () -> historial.miInforme(e.token("paciente1"), 99));
        assertThrows(AccesoDenegadoException.class, () -> historial.miInforme(e.token("paciente2"), eco));
    }

    @Test
    @DisplayName("CU13: un médico sin paciente asociado no tiene historial propio")
    void medicoSinPaciente() {
        assertThrows(ReglaNegocioException.class, () -> historial.miHistorial(e.token("medico1")));
    }

    @ParameterizedTest(name = "{0} no consulta historiales de paciente")
    @ValueSource(strings = { "admision1", "admin1" })
    void sinPermiso(String login) {
        String t = e.token(login);
        assertThrows(AccesoDenegadoException.class, () -> historial.miHistorial(t));
        assertThrows(AccesoDenegadoException.class, () -> historial.miInforme(t, eco));
    }
}
