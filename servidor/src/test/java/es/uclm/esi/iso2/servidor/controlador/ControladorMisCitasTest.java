package es.uclm.esi.iso2.servidor.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import es.uclm.esi.iso2.servidor.api.ApiMisCitas;
import es.uclm.esi.iso2.servidor.catalogo.Modalidad;
import es.uclm.esi.iso2.servidor.catalogo.Prioridad;
import es.uclm.esi.iso2.servidor.citas.EstadoCita;
import es.uclm.esi.iso2.servidor.comun.AccesoDenegadoException;
import es.uclm.esi.iso2.servidor.comun.ReglaNegocioException;
import es.uclm.esi.iso2.servidor.seguridad.Rol;
import es.uclm.esi.iso2.servidor.solicitudes.EstadoSolicitud;

/** CU07 · Gestionar mis citas. */
class ControladorMisCitasTest {

    private final Entorno e = new Entorno().conDatosBasicos();
    private final ApiMisCitas api = e.servidor.misCitas();
    private long cita;
    private long solicitud;
    private long agenda;

    @BeforeEach
    void preparar() {
        solicitud = e.solicitar("medico1", "PAC-1", "RX-T", Prioridad.ORDINARIO);
        agenda = e.agenda(Modalidad.RX, "RX1");
        cita = e.citar(solicitud, agenda, "09:00");
    }

    @Test
    @DisplayName("El paciente ve sus citas, confirma y pide un cambio que ve admisión")
    void confirmarYPedirCambio() {
        String t = e.token("paciente1");
        assertEquals(1, api.misCitas(t).size());
        assertEquals(EstadoCita.CONFIRMADA, api.confirmar(t, cita).getEstado());
        assertEquals(EstadoCita.CAMBIO_SOLICITADO, api.solicitarCambio(t, cita, "Tengo un examen").getEstado());
        assertEquals(1, e.servidor.citas().conCambioSolicitado(e.token("admision1")).size());
        e.servidor.citas().reprogramar(e.token("admision1"), cita, agenda, LocalTime.of(9, 30));
        assertEquals(EstadoCita.PROGRAMADA, api.misCitas(t).get(0).getEstado());
    }

    @Test
    @DisplayName("El paciente cancela: hueco libre y solicitud de vuelta en la lista de espera")
    void cancelar() {
        String t = e.token("paciente1");
        api.cancelar(t, cita, "Ya me la hicieron en otro centro");
        assertEquals(EstadoCita.CANCELADA, api.misCitas(t).get(0).getEstado());
        assertEquals(EstadoSolicitud.SOLICITADA,
                e.servidor.solicitudes().obtener(e.token("admision1"), solicitud).getEstado());
    }

    @Test
    @DisplayName("Un paciente no puede operar sobre la cita de otro")
    void citaAjena() {
        String t = e.token("paciente2");
        assertTrue(api.misCitas(t).isEmpty());
        assertThrows(AccesoDenegadoException.class, () -> api.confirmar(t, cita));
        assertThrows(AccesoDenegadoException.class, () -> api.cancelar(t, cita, "x"));
        assertThrows(AccesoDenegadoException.class, () -> api.solicitarCambio(t, cita, "x"));
    }

    @Test
    @DisplayName("Valor límite: en el momento de la cita ya no se puede confirmar ni cancelar")
    void citaPasada() {
        e.reloj.fijar(Entorno.MANANA.atTime(8, 59));
        assertEquals(EstadoCita.CONFIRMADA, api.confirmar(e.token("paciente1"), cita).getEstado());
        e.reloj.avanzar(Duration.ofMinutes(1));
        String t = e.token("paciente1");
        assertThrows(ReglaNegocioException.class, () -> api.cancelar(t, cita, "tarde"));
        assertThrows(ReglaNegocioException.class, () -> api.solicitarCambio(t, cita, "tarde"));
    }

    @Test
    @DisplayName("D-005: un médico sin paciente asociado recibe un error claro; con paciente asociado, puede")
    void medico() {
        String t = e.token("medico1");
        assertThrows(ReglaNegocioException.class, () -> api.misCitas(t));
        e.servidor.registrarUsuario("medico3", Entorno.CLAVE, "Dr. Paciente", Rol.MEDICO, "PAC-1");
        assertEquals(1, api.misCitas(e.token("medico3")).size());
    }

    @ParameterizedTest(name = "{0} no tiene «mis citas»")
    @ValueSource(strings = { "admision1", "admin1" })
    void sinPermiso(String login) {
        assertThrows(AccesoDenegadoException.class, () -> api.misCitas(e.token(login)));
    }
}
