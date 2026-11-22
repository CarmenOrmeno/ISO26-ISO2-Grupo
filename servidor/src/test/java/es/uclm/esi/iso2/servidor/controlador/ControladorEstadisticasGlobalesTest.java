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

import es.uclm.esi.iso2.servidor.api.ApiEstadisticasGlobales;
import es.uclm.esi.iso2.servidor.atencion.CategoriaIncidencia;
import es.uclm.esi.iso2.servidor.catalogo.Modalidad;
import es.uclm.esi.iso2.servidor.catalogo.Prioridad;
import es.uclm.esi.iso2.servidor.comun.AccesoDenegadoException;
import es.uclm.esi.iso2.servidor.estadisticas.EstadisticasGlobales;
import es.uclm.esi.iso2.servidor.seguridad.Rol;

/** It13 · CU22 Generar estadísticas globales y reportes de uso. */
class ControladorEstadisticasGlobalesTest {

    private static final LocalDate HOY = Entorno.INICIO.toLocalDate();
    private static final LocalDate MANANA = Entorno.MANANA;

    private final Entorno e = new Entorno().conDatosBasicos();
    private final ApiEstadisticasGlobales globales = e.servidor.estadisticasGlobales();

    /**
     * Hoy se piden una Rx (PAC-1) y una eco (PAC-2). Mañana se realizan las dos, se informa la eco, los
     * pacientes valoran con 4 y 2, PAC-1 comunica una incidencia y admisión intenta ver un informe (denegado).
     */
    @BeforeEach
    void preparar() {
        long rx = e.solicitar("medico1", "PAC-1", "RX-T", Prioridad.ORDINARIO);
        long eco = e.solicitar("medico2", "PAC-2", "US-A", Prioridad.ORDINARIO);
        long citaRx = e.citar(rx, e.agenda(Modalidad.RX, "RX1", "medico1"), "09:00");
        long citaEco = e.citar(eco, e.agenda(Modalidad.US, "ECO1", "medico2"), "09:00");
        e.reloj.fijar(MANANA.atTime(10, 0));
        e.servidor.realizacion().registrarRealizacion(e.token("medico1"), citaRx, 1, "");
        e.servidor.realizacion().registrarRealizacion(e.token("medico2"), citaEco, 1, "");
        e.servidor.informes().crearInforme(e.token("medico2"), eco, "Hallazgos", "Normal");
        e.servidor.atencionPaciente().responderEncuesta(e.token("paciente1"), rx, 4, "");
        e.servidor.atencionPaciente().responderEncuesta(e.token("paciente2"), eco, 2, "");
        e.servidor.atencionPaciente().comunicarIncidencia(e.token("paciente1"), CategoriaIncidencia.OTRA, "Texto", null);
        String admision = e.token("admision1");
        assertThrows(AccesoDenegadoException.class, () -> e.servidor.informes().informeDeSolicitud(admision, eco));
    }

    private EstadisticasGlobales de(LocalDate desde, LocalDate hasta) {
        return globales.estadisticas(e.token("admin1"), desde, hasta);
    }

    @Test
    @DisplayName("CU22: actividad del periodo: solicitudes, citas, pruebas realizadas e informes")
    void actividad() {
        EstadisticasGlobales g = de(HOY, MANANA);
        assertEquals(2, g.solicitudes());
        assertEquals(2, g.citas());
        assertEquals(2, g.pruebasRealizadas());
        assertEquals(1, g.informesEmitidos());
    }

    @Test
    @DisplayName("CU22: situación actual: pacientes activos, usuarios por rol e incidencias abiertas")
    void situacion() {
        EstadisticasGlobales g = de(HOY, MANANA);
        assertEquals(2, g.pacientesActivos());
        assertEquals(Map.of(Rol.PACIENTE, 2L, Rol.MEDICO, 2L, Rol.ADMINISTRATIVO, 1L, Rol.ADMINISTRADOR, 1L),
                g.usuariosPorRol());
        assertEquals(1, g.incidenciasAbiertas());
    }

    @Test
    @DisplayName("CU22: satisfacción media de las encuestas del periodo (4 y 2 → 3,0)")
    void satisfaccion() {
        EstadisticasGlobales g = de(HOY, MANANA);
        assertEquals(2, g.encuestas());
        assertEquals(3.0, g.satisfaccionMedia().getAsDouble());
        assertTrue(g.toString().contains("satisfacción media: 3.0/5"));
    }

    @Test
    @DisplayName("CU22: uso: sesiones desde el arranque y accesos auditados y denegados (CU18)")
    void uso() {
        EstadisticasGlobales antes = de(HOY, MANANA);
        assertEquals(1, antes.accesosDenegados());
        assertEquals(e.servidor.auditoria().registros(e.token("admin1"), HOY, MANANA).size(), antes.accesosAuditados());
        e.token("paciente1");
        // tres sesiones más: la del administrador para la auditoría, la del paciente y la de esta consulta
        assertEquals(antes.sesionesIniciadas() + 3, de(HOY, MANANA).sesionesIniciadas());
    }

    @Test
    @DisplayName("CU22: valores límite del periodo: solo hoy (sin citas ni encuestas) y hasta < desde")
    void periodo() {
        EstadisticasGlobales hoy = de(HOY, HOY);
        assertEquals(2, hoy.solicitudes());
        assertEquals(0, hoy.citas());
        assertEquals(0, hoy.pruebasRealizadas());
        assertEquals(0, hoy.encuestas());
        assertTrue(hoy.satisfaccionMedia().isEmpty());
        assertTrue(hoy.toString().contains("sin datos"));
        assertThrows(IllegalArgumentException.class, () -> de(MANANA, HOY));
    }

    @ParameterizedTest(name = "{0} no consulta estadísticas globales")
    @ValueSource(strings = { "paciente1", "medico1", "admision1" })
    void sinPermiso(String login) {
        String t = e.token(login);
        assertThrows(AccesoDenegadoException.class, () -> globales.estadisticas(t, HOY, MANANA));
    }
}
