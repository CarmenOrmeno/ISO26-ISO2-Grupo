package es.uclm.esi.iso2.servidor.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import es.uclm.esi.iso2.servidor.api.ApiEstadisticasMedico;
import es.uclm.esi.iso2.servidor.catalogo.Modalidad;
import es.uclm.esi.iso2.servidor.catalogo.Prioridad;
import es.uclm.esi.iso2.servidor.comun.AccesoDenegadoException;
import es.uclm.esi.iso2.servidor.estadisticas.EstadisticasMedico;
import es.uclm.esi.iso2.servidor.seguimiento.DatosSeguimiento;
import es.uclm.esi.iso2.servidor.solicitudes.EstadoSolicitud;

/** It13 · CU20 Consultar estadísticas del médico. */
class ControladorEstadisticasMedicoTest {

    private static final LocalDate HOY = Entorno.INICIO.toLocalDate();
    private static final LocalDate MANANA = Entorno.MANANA;

    private final Entorno e = new Entorno().conDatosBasicos();
    private final ApiEstadisticasMedico estadisticas = e.servidor.estadisticasMedico();

    /**
     * Hoy, medico1 pide: Rx ORDINARIO de PAC-1 (realizada mañana: 1 día de espera), eco PREFERENTE de PAC-2
     * (citada) y TAC ORDINARIO de PAC-1 (sin citar); prescribe un seguimiento de eco a PAC-1 cuya primera
     * revisión (hoy) se genera y no se realiza. medico2 pide una Rx de PAC-2.
     */
    @BeforeEach
    void preparar() {
        long rx = e.solicitar("medico1", "PAC-1", "RX-T", Prioridad.ORDINARIO);
        long eco = e.solicitar("medico1", "PAC-2", "US-A", Prioridad.PREFERENTE);
        e.solicitar("medico1", "PAC-1", "TC-C", Prioridad.ORDINARIO);
        e.solicitar("medico2", "PAC-2", "RX-T", Prioridad.URGENTE);
        e.servidor.seguimiento().prescribir(e.token("medico1"), new DatosSeguimiento("PAC-1", "US-A", "Control",
                "Evolución", Prioridad.ORDINARIO, 6, 2, HOY));
        e.servidor.seguimiento().generarRevisiones(e.token("admin1"));
        long citaRx = e.citar(rx, e.agenda(Modalidad.RX, "RX1"), "09:00");
        e.citar(eco, e.agenda(Modalidad.US, "ECO1", "medico2"), "09:00");
        e.reloj.fijar(MANANA.atTime(9, 10));
        e.servidor.realizacion().registrarRealizacion(e.token("medico1"), citaRx, 1, "");
    }

    private EstadisticasMedico de(String medico, LocalDate desde, LocalDate hasta) {
        return estadisticas.misEstadisticas(e.token(medico), desde, hasta);
    }

    @Test
    @DisplayName("CU20: solicitudes por estado y por modalidad (uso de recursos)")
    void solicitudes() {
        EstadisticasMedico m = de("medico1", HOY, MANANA);
        assertEquals(4, m.totalSolicitudes());
        assertEquals(1L, m.porEstado().get(EstadoSolicitud.REALIZADA));
        assertEquals(1L, m.porEstado().get(EstadoSolicitud.CITADA));
        assertEquals(2L, m.porEstado().get(EstadoSolicitud.SOLICITADA));
        assertEquals(0L, m.porEstado().get(EstadoSolicitud.CANCELADA));
        assertEquals(Map.of(Modalidad.RX, 1L, Modalidad.US, 2L, Modalidad.TC, 1L), m.porModalidad());
    }

    @Test
    @DisplayName("CU20: espera media de solicitud a realización, solo de las prioridades con pruebas realizadas")
    void espera() {
        assertEquals(Map.of(Prioridad.ORDINARIO, 1.0), de("medico1", HOY, MANANA).esperaMediaDias());
    }

    @Test
    @DisplayName("CU20: adherencia a seguimientos = revisiones realizadas / generadas")
    void adherencia() {
        EstadisticasMedico m = de("medico1", HOY, MANANA);
        assertEquals(1, m.revisionesGeneradas());
        assertEquals(0, m.revisionesRealizadas());
        assertEquals(0.0, m.adherenciaSeguimientos().getAsDouble());
        assertTrue(m.toString().contains("Adherencia a seguimientos: 0.0 %"));
    }

    @Test
    @DisplayName("CU20: cada médico ve solo sus solicitudes; sin revisiones la adherencia no se puede calcular")
    void soloLasPropias() {
        EstadisticasMedico m = de("medico2", HOY, MANANA);
        assertEquals(1, m.totalSolicitudes());
        assertTrue(m.esperaMediaDias().isEmpty());
        assertTrue(m.adherenciaSeguimientos().isEmpty());
        assertTrue(m.toString().contains("sin datos"));
    }

    @Test
    @DisplayName("CU20: valores límite del periodo: el día siguiente no tiene solicitudes; hasta < desde no vale")
    void periodo() {
        assertEquals(4, de("medico1", HOY, HOY).totalSolicitudes());
        assertEquals(0, de("medico1", MANANA, MANANA).totalSolicitudes());
        assertEquals(0, de("medico1", HOY.minusDays(7), HOY.minusDays(1)).totalSolicitudes());
        assertThrows(IllegalArgumentException.class, () -> de("medico1", MANANA, HOY));
    }

    @ParameterizedTest(name = "{0} no consulta estadísticas de médico")
    @ValueSource(strings = { "paciente1", "admision1", "admin1" })
    void sinPermiso(String login) {
        String t = e.token(login);
        assertThrows(AccesoDenegadoException.class, () -> estadisticas.misEstadisticas(t, HOY, MANANA));
    }
}
