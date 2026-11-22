package es.uclm.esi.iso2.servidor.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import es.uclm.esi.iso2.servidor.api.ApiAtencion;
import es.uclm.esi.iso2.servidor.api.ApiAtencionPaciente;
import es.uclm.esi.iso2.servidor.atencion.CategoriaIncidencia;
import es.uclm.esi.iso2.servidor.atencion.Encuesta;
import es.uclm.esi.iso2.servidor.atencion.EstadoIncidencia;
import es.uclm.esi.iso2.servidor.atencion.IncidenciaPaciente;
import es.uclm.esi.iso2.servidor.catalogo.Modalidad;
import es.uclm.esi.iso2.servidor.catalogo.Prioridad;
import es.uclm.esi.iso2.servidor.comun.AccesoDenegadoException;
import es.uclm.esi.iso2.servidor.comun.NoEncontradoException;
import es.uclm.esi.iso2.servidor.comun.ReglaNegocioException;
import es.uclm.esi.iso2.servidor.solicitudes.SolicitudPrueba;

/** It11 · CU15 Comunicar incidencias y responder encuestas de satisfacción. */
class ControladorAtencionTest {

    private final Entorno e = new Entorno().conDatosBasicos();
    private final ApiAtencionPaciente paciente = e.servidor.atencionPaciente();
    private final ApiAtencion admision = e.servidor.atencion();
    private long realizada;
    private long informada;
    private long pendiente;

    /** PAC-1: Rx realizada (medico1), eco realizada e informada (medico2) y TAC sin realizar. */
    @BeforeEach
    void preparar() {
        realizada = e.solicitar("medico1", "PAC-1", "RX-T", Prioridad.ORDINARIO);
        informada = e.solicitar("medico1", "PAC-1", "US-A", Prioridad.ORDINARIO);
        pendiente = e.solicitar("medico1", "PAC-1", "TC-C", Prioridad.ORDINARIO);
        long citaRx = e.citar(realizada, e.agenda(Modalidad.RX, "RX1"), "09:00");
        long citaEco = e.citar(informada, e.agenda(Modalidad.US, "ECO1", "medico2"), "09:30");
        e.reloj.fijar(Entorno.MANANA.atTime(10, 0));
        e.servidor.realizacion().registrarRealizacion(e.token("medico1"), citaRx, 1, "");
        e.servidor.realizacion().registrarRealizacion(e.token("medico2"), citaEco, 2, "");
        e.servidor.informes().crearInforme(e.token("medico2"), informada, "Hígado homogéneo", "Normal");
    }

    @Test
    @DisplayName("CU15: el paciente comunica una incidencia general y otra sobre una prueba suya")
    void comunicar() {
        String t = e.token("paciente1");
        IncidenciaPaciente general = paciente.comunicarIncidencia(t, CategoriaIncidencia.INSTALACIONES,
                "No funciona el ascensor", null);
        IncidenciaPaciente sobrePrueba = paciente.comunicarIncidencia(t, CategoriaIncidencia.CITA,
                "Esperé una hora", realizada);
        assertEquals(EstadoIncidencia.ABIERTA, general.getEstado());
        assertEquals(realizada, sobrePrueba.getIdSolicitud());
        assertEquals(Entorno.MANANA.atTime(10, 0), sobrePrueba.getFecha());
        // la más reciente primero
        assertEquals(List.of(sobrePrueba.getId(), general.getId()),
                paciente.misIncidencias(t).stream().map(IncidenciaPaciente::getId).toList());
    }

    @Test
    @DisplayName("CU15: incidencia sobre una prueba ajena → AccesoDenegado; inexistente → NoEncontrado; sin texto → inválida")
    void incidenciaInvalida() {
        long ajena = e.solicitar("medico1", "PAC-2", "RX-T", Prioridad.ORDINARIO);
        String t = e.token("paciente1");
        assertThrows(AccesoDenegadoException.class,
                () -> paciente.comunicarIncidencia(t, CategoriaIncidencia.INFORME, "Texto", ajena));
        assertThrows(NoEncontradoException.class,
                () -> paciente.comunicarIncidencia(t, CategoriaIncidencia.INFORME, "Texto", 999L));
        assertThrows(IllegalArgumentException.class,
                () -> paciente.comunicarIncidencia(t, CategoriaIncidencia.OTRA, " ", null));
        assertTrue(paciente.misIncidencias(t).isEmpty());
    }

    @Test
    @DisplayName("CU15: cada paciente ve solo sus incidencias")
    void soloLasPropias() {
        paciente.comunicarIncidencia(e.token("paciente1"), CategoriaIncidencia.OTRA, "Texto", null);
        assertTrue(paciente.misIncidencias(e.token("paciente2")).isEmpty());
    }

    @Test
    @DisplayName("CU15: admisión ve las abiertas por orden de llegada y las responde una sola vez")
    void responderIncidencia() {
        long primera = paciente.comunicarIncidencia(e.token("paciente1"), CategoriaIncidencia.CITA, "Uno", null)
                .getId();
        long segunda = paciente.comunicarIncidencia(e.token("paciente2"), CategoriaIncidencia.OTRA, "Dos", null)
                .getId();
        String a = e.token("admision1");
        assertEquals(List.of(primera, segunda),
                admision.incidenciasAbiertas(a).stream().map(IncidenciaPaciente::getId).toList());

        IncidenciaPaciente respondida = admision.responderIncidencia(a, primera, "Lo sentimos");
        assertEquals("admision1", respondida.getRespondidaPor());
        assertEquals(List.of(segunda), admision.incidenciasAbiertas(a).stream().map(IncidenciaPaciente::getId).toList());
        assertEquals("Lo sentimos", paciente.misIncidencias(e.token("paciente1")).get(0).getRespuesta());
        assertThrows(ReglaNegocioException.class, () -> admision.responderIncidencia(a, primera, "Otra vez"));
        assertThrows(NoEncontradoException.class, () -> admision.responderIncidencia(a, 999, "Texto"));
    }

    @Test
    @DisplayName("CU15: encuestas pendientes = pruebas realizadas o informadas sin valorar")
    void pendientes() {
        String t = e.token("paciente1");
        assertEquals(List.of(realizada, informada), ids(paciente.encuestasPendientes(t)));
        paciente.responderEncuesta(t, informada, 5, "Muy bien");
        assertEquals(List.of(realizada), ids(paciente.encuestasPendientes(t)));
        assertTrue(paciente.encuestasPendientes(e.token("paciente2")).isEmpty());
    }

    @Test
    @DisplayName("CU15: el paciente valora su prueba una sola vez y admisión ve la encuesta")
    void responderEncuesta() {
        String t = e.token("paciente1");
        Encuesta encuesta = paciente.responderEncuesta(t, realizada, 4, "Puntuales");
        assertEquals(4, encuesta.puntuacion());
        assertEquals(List.of(encuesta), paciente.misEncuestas(t));
        assertEquals(List.of(encuesta), admision.encuestas(e.token("admision1")));
        assertThrows(ReglaNegocioException.class, () -> paciente.responderEncuesta(t, realizada, 5, ""));
    }

    @Test
    @DisplayName("CU15: no se valora una prueba sin realizar, ajena o inexistente, ni con puntuación fuera de 1..5")
    void encuestaInvalida() {
        String t = e.token("paciente1");
        assertThrows(ReglaNegocioException.class, () -> paciente.responderEncuesta(t, pendiente, 3, ""));
        assertThrows(AccesoDenegadoException.class,
                () -> paciente.responderEncuesta(e.token("paciente2"), realizada, 3, ""));
        assertThrows(NoEncontradoException.class, () -> paciente.responderEncuesta(t, 999, 3, ""));
        assertThrows(IllegalArgumentException.class, () -> paciente.responderEncuesta(t, realizada, 0, ""));
        assertThrows(IllegalArgumentException.class, () -> paciente.responderEncuesta(t, realizada, 6, ""));
        assertTrue(paciente.misEncuestas(t).isEmpty());
    }

    @Test
    @DisplayName("CU15: un médico sin paciente asociado no comunica incidencias propias")
    void medicoSinPaciente() {
        assertThrows(ReglaNegocioException.class, () -> paciente.misIncidencias(e.token("medico1")));
    }

    @ParameterizedTest(name = "{0} no usa la atención al paciente")
    @ValueSource(strings = { "admision1", "admin1" })
    void pacienteSinPermiso(String login) {
        String t = e.token(login);
        assertThrows(AccesoDenegadoException.class,
                () -> paciente.comunicarIncidencia(t, CategoriaIncidencia.OTRA, "Texto", null));
        assertThrows(AccesoDenegadoException.class, () -> paciente.responderEncuesta(t, realizada, 3, ""));
    }

    @ParameterizedTest(name = "{0} no atiende incidencias")
    @ValueSource(strings = { "paciente1", "medico1", "admin1" })
    void admisionSinPermiso(String login) {
        String t = e.token(login);
        assertThrows(AccesoDenegadoException.class, () -> admision.incidenciasAbiertas(t));
        assertThrows(AccesoDenegadoException.class, () -> admision.responderIncidencia(t, 1, "Texto"));
        assertThrows(AccesoDenegadoException.class, () -> admision.encuestas(t));
    }

    private static List<Long> ids(List<SolicitudPrueba> solicitudes) {
        return solicitudes.stream().map(SolicitudPrueba::getId).toList();
    }
}
