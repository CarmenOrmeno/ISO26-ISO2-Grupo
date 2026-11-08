package es.uclm.esi.iso2.servidor.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import es.uclm.esi.iso2.servidor.api.ApiCitas;
import es.uclm.esi.iso2.servidor.catalogo.Modalidad;
import es.uclm.esi.iso2.servidor.catalogo.Prioridad;
import es.uclm.esi.iso2.servidor.citas.Cita;
import es.uclm.esi.iso2.servidor.citas.EstadoCita;
import es.uclm.esi.iso2.servidor.comun.AccesoDenegadoException;
import es.uclm.esi.iso2.servidor.comun.NoEncontradoException;
import es.uclm.esi.iso2.servidor.comun.ReglaNegocioException;
import es.uclm.esi.iso2.servidor.solicitudes.EstadoSolicitud;

/** Pruebas de integración de CU06 con servicios reales (citas + solicitudes + agendas + catálogo). */
class ControladorCitasTest {

    private final Entorno e = new Entorno().conDatosBasicos();
    private final ApiCitas api = e.servidor.citas();
    private String t;
    private long solicitudRx;
    private long agendaRx;

    @BeforeEach
    void preparar() {
        t = e.token("admision1");
        solicitudRx = e.solicitar("medico1", "PAC-1", "RX-T", Prioridad.ORDINARIO);
        agendaRx = e.agenda(Modalidad.RX, "RX1");
    }

    @Test
    @DisplayName("CU06 asignar: ocupa el hueco y la solicitud pasa a CITADA")
    void asignar() {
        assertEquals(1, api.solicitudesPendientes(t).size());
        Cita c = api.asignar(t, solicitudRx, agendaRx, LocalTime.of(9, 15));
        assertEquals(Entorno.MANANA.atTime(9, 15), c.getFechaHora());
        assertEquals(EstadoSolicitud.CITADA, e.servidor.solicitudes().obtener(t, solicitudRx).getEstado());
        assertFalse(e.servidor.agendas().obtener(t, agendaRx).hueco(LocalTime.of(9, 15)).libre());
        assertTrue(api.solicitudesPendientes(t).isEmpty());
        assertEquals(1, api.delDia(t, Entorno.MANANA).size());
        assertEquals(1, api.dePaciente(t, "PAC-1").size());
        assertEquals(c.getId(), api.obtener(t, c.getId()).getId());
    }

    @Test
    @DisplayName("CU06 reprogramar: libera el hueco anterior y ocupa el nuevo")
    void reprogramar() {
        long id = e.citar(solicitudRx, agendaRx, "09:00");
        Cita c = api.reprogramar(t, id, agendaRx, LocalTime.of(9, 45));
        assertEquals(1, c.getReprogramaciones());
        assertTrue(e.servidor.agendas().obtener(t, agendaRx).hueco(LocalTime.of(9, 0)).libre());
        assertFalse(e.servidor.agendas().obtener(t, agendaRx).hueco(LocalTime.of(9, 45)).libre());
    }

    @Test
    @DisplayName("CU06 cancelar: libera el hueco y la solicitud vuelve a la lista de espera")
    void cancelar() {
        long id = e.citar(solicitudRx, agendaRx, "09:00");
        api.cancelar(t, id, "Paciente ingresado");
        assertEquals(EstadoCita.CANCELADA, api.obtener(t, id).getEstado());
        assertTrue(e.servidor.agendas().obtener(t, agendaRx).hueco(LocalTime.of(9, 0)).libre());
        assertEquals(1, api.solicitudesPendientes(t).size());
        assertThrows(ReglaNegocioException.class, () -> api.reprogramar(t, id, agendaRx, LocalTime.of(9, 30)));
        long otra = e.citar(solicitudRx, agendaRx, "09:00");
        assertEquals(2, otra);
    }

    @Test
    @DisplayName("Conjetura de errores al asignar")
    void erroresAsignar() {
        long agendaTc = e.agenda(Modalidad.TC, "TC1", "medico2");
        assertThrows(ReglaNegocioException.class, () -> api.asignar(t, solicitudRx, agendaTc, LocalTime.of(9, 0)),
                "modalidad distinta");
        assertThrows(NoEncontradoException.class, () -> api.asignar(t, solicitudRx, agendaRx, LocalTime.of(9, 10)),
                "hora sin hueco");
        assertThrows(NoEncontradoException.class, () -> api.asignar(t, 99, agendaRx, LocalTime.of(9, 0)));
        e.citar(solicitudRx, agendaRx, "09:00");
        assertThrows(ReglaNegocioException.class, () -> api.asignar(t, solicitudRx, agendaRx, LocalTime.of(9, 15)),
                "solicitud ya citada");
        long otraRx = e.solicitar("medico1", "PAC-2", "RX-T", Prioridad.URGENTE);
        assertThrows(ReglaNegocioException.class, () -> api.asignar(t, otraRx, agendaRx, LocalTime.of(9, 0)),
                "hueco ocupado");
        assertThrows(ReglaNegocioException.class, () -> api.reprogramar(t, 1, agendaRx, LocalTime.of(9, 0)),
                "reprogramar a un hueco ocupado (el suyo)");
    }

    @Test
    @DisplayName("El paciente no puede tener dos citas activas a la misma hora (en salas distintas)")
    void mismaHora() {
        long agendaRx2 = e.servidor.agendas().crear(t, new es.uclm.esi.iso2.servidor.agendas.DatosAgenda(
                Entorno.MANANA, "RX2", Modalidad.US, "medico2", LocalTime.of(9, 0), LocalTime.of(10, 0), 15)).getId();
        long eco = e.solicitar("medico1", "PAC-1", "US-A", Prioridad.ORDINARIO);
        long citaRx = e.citar(solicitudRx, agendaRx, "09:00");
        assertThrows(ReglaNegocioException.class, () -> api.asignar(t, eco, agendaRx2, LocalTime.of(9, 0)));
        api.asignar(t, eco, agendaRx2, LocalTime.of(9, 15));
        api.reprogramar(t, citaRx, agendaRx, LocalTime.of(9, 0).plusMinutes(30));
    }

    @Test
    @DisplayName("Valores límite del momento: no se cita en el pasado ni en el instante actual")
    void pasado() {
        e.reloj.fijar(Entorno.MANANA.atTime(9, 15));
        String t = e.token("admision1"); // la sesión anterior ha caducado (CU01)
        assertThrows(ReglaNegocioException.class, () -> api.asignar(t, solicitudRx, agendaRx, LocalTime.of(9, 0)));
        assertThrows(ReglaNegocioException.class, () -> api.asignar(t, solicitudRx, agendaRx, LocalTime.of(9, 15)));
        e.reloj.avanzar(Duration.ofMinutes(-1));
        String t2 = e.token("admision1");
        api.asignar(t2, solicitudRx, agendaRx, LocalTime.of(9, 15));
    }

    @ParameterizedTest(name = "{0} no gestiona citas")
    @ValueSource(strings = { "paciente1", "medico1", "admin1" })
    void sinPermiso(String login) {
        String otro = e.token(login);
        assertThrows(AccesoDenegadoException.class, () -> api.solicitudesPendientes(otro));
        assertThrows(AccesoDenegadoException.class, () -> api.asignar(otro, solicitudRx, agendaRx, LocalTime.of(9, 0)));
        assertThrows(AccesoDenegadoException.class, () -> api.cancelar(otro, 1, "x"));
        assertThrows(AccesoDenegadoException.class, () -> api.reprogramar(otro, 1, agendaRx, LocalTime.of(9, 0)));
        assertThrows(AccesoDenegadoException.class, () -> api.obtener(otro, 1));
        assertThrows(AccesoDenegadoException.class, () -> api.delDia(otro, Entorno.MANANA));
        assertThrows(AccesoDenegadoException.class, () -> api.dePaciente(otro, "PAC-1"));
    }
}
