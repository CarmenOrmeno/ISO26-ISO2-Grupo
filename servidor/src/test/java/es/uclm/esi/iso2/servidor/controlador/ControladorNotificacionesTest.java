package es.uclm.esi.iso2.servidor.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import es.uclm.esi.iso2.servidor.api.ApiNotificaciones;
import es.uclm.esi.iso2.servidor.catalogo.Modalidad;
import es.uclm.esi.iso2.servidor.catalogo.Prioridad;
import es.uclm.esi.iso2.servidor.comun.AccesoDenegadoException;
import es.uclm.esi.iso2.servidor.notificaciones.Notificacion;
import es.uclm.esi.iso2.servidor.notificaciones.TipoNotificacion;

/** CU12 · Notificar y recordar. */
class ControladorNotificacionesTest {

    private final Entorno e = new Entorno().conDatosBasicos();
    private final ApiNotificaciones api = e.servidor.notificaciones();

    private List<TipoNotificacion> tipos(String login) {
        return api.misNotificaciones(e.token(login)).stream().map(Notificacion::getTipo).toList();
    }

    @Test
    @DisplayName("Asignar, reprogramar y cancelar una cita avisan al paciente (de más reciente a más antigua)")
    void avisosDeCitas() {
        long s = e.solicitar("medico1", "PAC-1", "RX-T", Prioridad.ORDINARIO);
        long a = e.agenda(Modalidad.RX, "RX1");
        long c = e.citar(s, a, "09:00");
        String t = e.token("admision1");
        e.servidor.citas().reprogramar(t, c, a, LocalTime.of(9, 15));
        e.servidor.citas().cancelar(t, c, "Avería del equipo");
        assertEquals(List.of(TipoNotificacion.CITA_CANCELADA, TipoNotificacion.CITA_MODIFICADA,
                TipoNotificacion.CITA_ASIGNADA), tipos("paciente1"));
        Notificacion asignada = api.misNotificaciones(e.token("paciente1")).get(2);
        assertTrue(asignada.getMensaje().contains("Rx tórax el " + Entorno.MANANA + " a las 09:00 (sala RX1)"));
        assertTrue(api.misNotificaciones(e.token("paciente2")).isEmpty());
    }

    @Test
    @DisplayName("Marcar como leída reduce el contador; una ajena no se puede marcar")
    void leidas() {
        e.citar(e.solicitar("medico1", "PAC-1", "RX-T", Prioridad.ORDINARIO), e.agenda(Modalidad.RX, "RX1"), "09:00");
        String t = e.token("paciente1");
        assertEquals(1, api.noLeidas(t));
        long id = api.misNotificaciones(t).get(0).getId();
        assertThrows(AccesoDenegadoException.class, () -> api.marcarLeida(e.token("paciente2"), id));
        api.marcarLeida(t, id);
        assertEquals(0, api.noLeidas(t));
    }

    @ParameterizedTest(name = "faltan {0} min para la cita (antelación 24 h) → recordadas={1}")
    @CsvSource({ "1441,0", "1440,1", "60,1", "0,0" })
    @DisplayName("Valores límite de la ventana de recordatorio (ahora, ahora + 24 h]")
    void ventanaRecordatorio(long minutosAntes, int esperadas) {
        e.citar(e.solicitar("medico1", "PAC-1", "RX-T", Prioridad.ORDINARIO), e.agenda(Modalidad.RX, "RX1"), "09:00");
        e.reloj.fijar(Entorno.MANANA.atTime(9, 0).minusMinutes(minutosAntes));
        assertEquals(esperadas, api.enviarRecordatorios(e.token("admin1")));
    }

    @Test
    @DisplayName("Cada cita se recuerda una vez; con preparación previa se avisa también de ella; las canceladas no")
    void recordatorios() {
        long eco = e.solicitar("medico1", "PAC-1", "US-A", Prioridad.ORDINARIO);
        long rx = e.solicitar("medico1", "PAC-2", "RX-T", Prioridad.ORDINARIO);
        long tc = e.solicitar("medico2", "PAC-2", "TC-C", Prioridad.ORDINARIO);
        e.citar(eco, e.agenda(Modalidad.US, "ECO1", "medico1"), "09:00");
        e.citar(rx, e.agenda(Modalidad.RX, "RX1", "medico2"), "09:00");
        long citaTc = e.citar(tc, e.servidor.agendas().crear(e.token("admision1"),
                new es.uclm.esi.iso2.servidor.agendas.DatosAgenda(Entorno.MANANA, "TC1", Modalidad.TC, "medico3",
                        LocalTime.of(11, 0), LocalTime.of(12, 0), 30)).getId(), "11:00");
        e.servidor.citas().cancelar(e.token("admision1"), citaTc, "No procede");
        // a las 9:00 del lunes las citas de las 9:00 del martes están justo a 24 h (límite incluido)
        String admin = e.token("admin1");
        assertEquals(2, api.enviarRecordatorios(admin));
        assertEquals(0, api.enviarRecordatorios(admin));
        assertTrue(tipos("paciente1").containsAll(List.of(TipoNotificacion.RECORDATORIO, TipoNotificacion.PREPARACION)));
        assertTrue(!tipos("paciente2").contains(TipoNotificacion.PREPARACION));
    }

    @ParameterizedTest(name = "{0} no ejecuta la tarea de recordatorios")
    @ValueSource(strings = { "paciente1", "medico1", "admision1" })
    void sinPermisoTarea(String login) {
        assertThrows(AccesoDenegadoException.class, () -> api.enviarRecordatorios(e.token(login)));
    }

    @ParameterizedTest(name = "{0} no tiene buzón de paciente")
    @ValueSource(strings = { "admision1", "admin1" })
    void sinBuzon(String login) {
        assertThrows(AccesoDenegadoException.class, () -> api.misNotificaciones(e.token(login)));
    }
}
