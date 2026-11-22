package es.uclm.esi.iso2.servidor.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import es.uclm.esi.iso2.servidor.api.ApiMisSeguimientos;
import es.uclm.esi.iso2.servidor.api.ApiSeguimiento;
import es.uclm.esi.iso2.servidor.catalogo.Prioridad;
import es.uclm.esi.iso2.servidor.comun.AccesoDenegadoException;
import es.uclm.esi.iso2.servidor.comun.NoEncontradoException;
import es.uclm.esi.iso2.servidor.comun.ReglaNegocioException;
import es.uclm.esi.iso2.servidor.notificaciones.TipoNotificacion;
import es.uclm.esi.iso2.servidor.seguimiento.DatosSeguimiento;
import es.uclm.esi.iso2.servidor.seguimiento.EstadoSeguimiento;
import es.uclm.esi.iso2.servidor.seguimiento.Seguimiento;
import es.uclm.esi.iso2.servidor.solicitudes.EstadoSolicitud;
import es.uclm.esi.iso2.servidor.solicitudes.SolicitudPrueba;

/** It9 · CU10 Prescribir seguimiento y CU16 Consultar seguimientos. */
class ControladorSeguimientoTest {

    private static final LocalDate HOY = Entorno.INICIO.toLocalDate();

    private final Entorno e = new Entorno().conDatosBasicos();
    private final ApiSeguimiento seguimiento = e.servidor.seguimiento();
    private final ApiMisSeguimientos misSeguimientos = e.servidor.misSeguimientos();

    private static DatosSeguimiento datos(String paciente, String tipo, LocalDate primera) {
        return new DatosSeguimiento(paciente, tipo, "Control de nódulo", "Evolución", Prioridad.ORDINARIO, 6, 2,
                primera);
    }

    private Seguimiento prescribir(LocalDate primera) {
        return seguimiento.prescribir(e.token("medico1"), datos("PAC-1", "US-A", primera));
    }

    private int generar() {
        return seguimiento.generarRevisiones(e.token("admin1"));
    }

    @Test
    @DisplayName("CU10: el médico prescribe un seguimiento y el paciente recibe aviso")
    void prescribir() {
        Seguimiento s = prescribir(HOY.plusMonths(1));
        assertEquals(EstadoSeguimiento.ACTIVO, s.getEstado());
        assertEquals("medico1", s.getLoginMedico());
        assertEquals(List.of(s.getId()),
                seguimiento.misPrescripciones(e.token("medico1")).stream().map(Seguimiento::getId).toList());
        assertTrue(seguimiento.misPrescripciones(e.token("medico2")).isEmpty());
        assertEquals(TipoNotificacion.SEGUIMIENTO,
                e.servidor.notificaciones().misNotificaciones(e.token("paciente1")).get(0).getTipo());
    }

    @Test
    @DisplayName("CU10: tipo que no admite seguimiento, paciente inexistente o tipo inexistente")
    void reglasDePrescripcion() {
        String t = e.token("medico1");
        assertThrows(ReglaNegocioException.class, () -> seguimiento.prescribir(t, datos("PAC-1", "RX-T", HOY)));
        assertThrows(ReglaNegocioException.class, () -> seguimiento.prescribir(t, datos("PAC-9", "US-A", HOY)));
        assertThrows(NoEncontradoException.class, () -> seguimiento.prescribir(t, datos("PAC-1", "XX", HOY)));
    }

    @Test
    @DisplayName("CU10: valor límite de la primera revisión, ayer no, hoy sí")
    void primeraRevision() {
        assertThrows(ReglaNegocioException.class, () -> prescribir(HOY.minusDays(1)));
        prescribir(HOY);
    }

    @Test
    @DisplayName("Temporizador: la revisión vencida genera una solicitud SOLICITADA vinculada al seguimiento")
    void generarRevision() {
        Seguimiento s = prescribir(HOY.plusDays(1));
        assertEquals(0, generar(), "aún no ha vencido");
        e.reloj.fijar(Entorno.MANANA.atStartOfDay());
        assertEquals(1, generar());
        assertEquals(0, generar(), "no se genera dos veces la misma revisión");

        List<SolicitudPrueba> revisiones = misSeguimientos.revisiones(e.token("paciente1"), s.getId());
        assertEquals(1, revisiones.size());
        SolicitudPrueba solicitud = revisiones.get(0);
        assertEquals(EstadoSolicitud.SOLICITADA, solicitud.getEstado());
        assertEquals(s.getId(), solicitud.getIdSeguimiento());
        assertEquals("medico1", solicitud.getLoginMedico());
        assertEquals(Entorno.MANANA.plusMonths(6),
                misSeguimientos.misSeguimientos(e.token("paciente1")).get(0).proximaRevision().orElseThrow());
    }

    @Test
    @DisplayName("Conjetura de errores: si la revisión anterior sigue abierta, la siguiente se aplaza")
    void revisionAnteriorAbierta() {
        Seguimiento s = prescribir(HOY);
        assertEquals(1, generar());
        e.reloj.fijar(Entorno.INICIO.plusMonths(6));
        assertEquals(0, generar(), "la primera solicitud sigue SOLICITADA");

        long primera = s.getSolicitudes().get(0);
        e.servidor.solicitudes().cancelar(e.token("medico1"), primera, "No acudió");
        assertEquals(1, generar());
        assertEquals(EstadoSeguimiento.FINALIZADO, e.servidor.seguimiento().misPrescripciones(e.token("medico1"))
                .get(0).getEstado());
        e.reloj.fijar(Entorno.INICIO.plusYears(3));
        assertEquals(0, generar(), "un seguimiento finalizado no genera más");
    }

    @Test
    @DisplayName("CU10: solo el médico que lo prescribió lo cancela; un cancelado no genera revisiones")
    void cancelar() {
        Seguimiento s = prescribir(HOY);
        assertThrows(AccesoDenegadoException.class,
                () -> seguimiento.cancelar(e.token("medico2"), s.getId(), "No es mío"));
        seguimiento.cancelar(e.token("medico1"), s.getId(), "Alta del proceso");
        assertEquals(0, generar());
        assertThrows(ReglaNegocioException.class,
                () -> seguimiento.cancelar(e.token("medico1"), s.getId(), "Otra vez"));
        assertThrows(NoEncontradoException.class, () -> seguimiento.cancelar(e.token("medico1"), 99, "x"));
    }

    @Test
    @DisplayName("CU16: el paciente solo ve sus seguimientos y no las revisiones de otro paciente")
    void consultarSoloLosPropios() {
        Seguimiento s = prescribir(HOY);
        seguimiento.prescribir(e.token("medico1"), datos("PAC-2", "TC-C", HOY));
        assertEquals(1, misSeguimientos.misSeguimientos(e.token("paciente1")).size());
        assertEquals(1, misSeguimientos.misSeguimientos(e.token("paciente2")).size());
        assertThrows(AccesoDenegadoException.class, () -> misSeguimientos.revisiones(e.token("paciente2"), s.getId()));
        assertThrows(NoEncontradoException.class, () -> misSeguimientos.revisiones(e.token("paciente1"), 99));
    }

    @Test
    @DisplayName("CU16: un médico sin paciente asociado no tiene seguimientos propios")
    void medicoSinPaciente() {
        assertThrows(ReglaNegocioException.class, () -> misSeguimientos.misSeguimientos(e.token("medico1")));
    }

    @ParameterizedTest(name = "{0} no prescribe seguimientos")
    @ValueSource(strings = { "paciente1", "admision1", "admin1" })
    void sinPermisoPrescribir(String login) {
        String t = e.token(login);
        assertThrows(AccesoDenegadoException.class, () -> seguimiento.prescribir(t, datos("PAC-1", "US-A", HOY)));
        assertThrows(AccesoDenegadoException.class, () -> seguimiento.misPrescripciones(t));
    }

    @ParameterizedTest(name = "{0} no ejecuta la tarea programada ni consulta seguimientos de paciente")
    @ValueSource(strings = { "admision1" })
    void sinPermisoConsultar(String login) {
        String t = e.token(login);
        assertThrows(AccesoDenegadoException.class, () -> seguimiento.generarRevisiones(t));
        assertThrows(AccesoDenegadoException.class, () -> misSeguimientos.misSeguimientos(t));
    }

    @Test
    @DisplayName("Solo el administrador ejecuta la tarea programada")
    void tareaProgramada() {
        assertThrows(AccesoDenegadoException.class, () -> seguimiento.generarRevisiones(e.token("medico1")));
        assertThrows(AccesoDenegadoException.class, () -> seguimiento.generarRevisiones(e.token("paciente1")));
    }
}
