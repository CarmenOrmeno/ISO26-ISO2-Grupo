package es.uclm.esi.iso2.servidor.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import es.uclm.esi.iso2.servidor.agendas.Agenda;
import es.uclm.esi.iso2.servidor.api.ApiEstadoSolicitudes;
import es.uclm.esi.iso2.servidor.catalogo.Modalidad;
import es.uclm.esi.iso2.servidor.catalogo.Prioridad;
import es.uclm.esi.iso2.servidor.citas.SituacionSolicitud;
import es.uclm.esi.iso2.servidor.comun.AccesoDenegadoException;
import es.uclm.esi.iso2.servidor.comun.NoEncontradoException;
import es.uclm.esi.iso2.servidor.solicitudes.EstadoSolicitud;
import es.uclm.esi.iso2.servidor.solicitudes.SolicitudPrueba;

/** It10 · CU14 Consultar el estado de solicitudes y las agendas disponibles. */
class ControladorEstadoSolicitudesTest {

    private final Entorno e = new Entorno().conDatosBasicos();
    private final ApiEstadoSolicitudes estado = e.servidor.estadoSolicitudes();

    private String med(String login) {
        return e.token(login);
    }

    @Test
    @DisplayName("CU14: el médico ve solo sus solicitudes y puede filtrarlas por estado")
    void misSolicitudes() {
        long citada = e.solicitar("medico1", "PAC-1", "RX-T", Prioridad.ORDINARIO);
        long pendiente = e.solicitar("medico1", "PAC-2", "US-A", Prioridad.PREFERENTE);
        e.solicitar("medico2", "PAC-1", "TC-C", Prioridad.URGENTE);
        e.citar(citada, e.agenda(Modalidad.RX, "RX1"), "09:00");

        assertEquals(2, estado.misSolicitudes(med("medico1")).size());
        assertEquals(1, estado.misSolicitudes(med("medico2")).size());
        assertEquals(List.of(citada), estado.misSolicitudes(med("medico1"), EstadoSolicitud.CITADA).stream()
                .map(SolicitudPrueba::getId).toList());
        assertEquals(List.of(pendiente), estado.misSolicitudes(med("medico1"), EstadoSolicitud.SOLICITADA).stream()
                .map(SolicitudPrueba::getId).toList());
        assertTrue(estado.misSolicitudes(med("medico1"), EstadoSolicitud.INFORMADA).isEmpty());
    }

    @Test
    @DisplayName("CU14: situación sin cita, con cita y, tras cancelarla y volver a citar, la cita vigente")
    void situacion() {
        long s = e.solicitar("medico1", "PAC-1", "RX-T", Prioridad.ORDINARIO);
        SituacionSolicitud sinCita = estado.situacion(med("medico1"), s);
        assertTrue(sinCita.cita().isEmpty());
        assertTrue(sinCita.toString().contains("Sin cita"));

        long agenda = e.agenda(Modalidad.RX, "RX1");
        long primera = e.citar(s, agenda, "09:00");
        assertEquals(primera, estado.situacion(med("medico1"), s).cita().orElseThrow().getId());

        e.servidor.citas().cancelar(e.token("admision1"), primera, "Avería del equipo");
        assertTrue(estado.situacion(med("medico1"), s).cita().isEmpty(), "la cancelada no es vigente");
        long segunda = e.citar(s, agenda, "09:30");
        SituacionSolicitud vigente = estado.situacion(med("medico1"), s);
        assertEquals(segunda, vigente.cita().orElseThrow().getId());
        assertEquals(EstadoSolicitud.CITADA, vigente.solicitud().getEstado());
    }

    @Test
    @DisplayName("RNF-S3: la situación de una solicitud ajena se deniega; una inexistente no se encuentra")
    void situacionAjena() {
        long s = e.solicitar("medico1", "PAC-1", "RX-T", Prioridad.ORDINARIO);
        assertThrows(AccesoDenegadoException.class, () -> estado.situacion(med("medico2"), s));
        assertThrows(NoEncontradoException.class, () -> estado.situacion(med("medico1"), 99));
    }

    @Test
    @DisplayName("CU14: el médico ve sus agendas del intervalo y los huecos libres de una modalidad")
    void agendas() {
        long suya = e.agenda(Modalidad.RX, "RX1", "medico1");
        e.agenda(Modalidad.US, "ECO1", "medico2");
        String t = med("medico1");
        assertEquals(List.of(suya),
                estado.misAgendas(t, Entorno.MANANA, Entorno.MANANA).stream().map(Agenda::getId).toList());
        assertTrue(estado.misAgendas(t, Entorno.MANANA.plusDays(1), Entorno.MANANA.plusDays(7)).isEmpty());
        assertEquals(4, estado.huecosLibres(t, Modalidad.RX, Entorno.MANANA, Entorno.MANANA).size());
        assertEquals(4, estado.huecosLibres(med("medico2"), Modalidad.US, Entorno.MANANA, Entorno.MANANA).size());
    }

    @Test
    @DisplayName("CU14: valor límite del intervalo, desde = hasta vale; hasta < desde no")
    void intervalo() {
        e.agenda(Modalidad.RX, "RX1", "medico1");
        String t = med("medico1");
        assertEquals(1, estado.misAgendas(t, Entorno.MANANA, Entorno.MANANA).size());
        assertThrows(IllegalArgumentException.class,
                () -> estado.misAgendas(t, Entorno.MANANA, Entorno.MANANA.minusDays(1)));
        assertThrows(IllegalArgumentException.class, () -> estado.misAgendas(t, null, Entorno.MANANA));
    }

    @ParameterizedTest(name = "{0} no consulta el estado de solicitudes de médico")
    @ValueSource(strings = { "paciente1", "admision1", "admin1" })
    void sinPermiso(String login) {
        String t = e.token(login);
        assertThrows(AccesoDenegadoException.class, () -> estado.misSolicitudes(t));
        assertThrows(AccesoDenegadoException.class, () -> estado.situacion(t, 1));
        assertThrows(AccesoDenegadoException.class, () -> estado.misAgendas(t, Entorno.MANANA, Entorno.MANANA));
        assertThrows(AccesoDenegadoException.class,
                () -> estado.huecosLibres(t, Modalidad.RX, Entorno.MANANA, Entorno.MANANA));
    }
}
