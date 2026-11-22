package es.uclm.esi.iso2.cliente.controlador;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.uclm.esi.iso2.servidor.Servidor;

/**
 * ItN · Prueba de integración de extremo a extremo: el ciclo de vida completo de una prueba diagnóstica a
 * través del cliente (las mismas acciones que usan las vistas), con los cuatro roles sobre un único servidor.
 * Recorre CU04 → CU05 → CU11 → CU07 → CU12 → CU08 → CU09 → CU13 → CU15 → CU19 → CU18 → CU20 → CU21 → CU22.
 */
class FlujoCompletoTest {

    /** Reloj que la prueba adelanta (lunes 02/11/2026 a las 9:00). */
    private static final class Reloj extends Clock {
        private final ZoneId zona = ZoneId.systemDefault();
        private Instant ahora = LocalDateTime.of(2026, 11, 2, 9, 0).atZone(zona).toInstant();

        void fijar(LocalDateTime momento) {
            ahora = momento.atZone(zona).toInstant();
        }

        @Override
        public ZoneId getZone() {
            return zona;
        }

        @Override
        public Clock withZone(ZoneId z) {
            return this;
        }

        @Override
        public Instant instant() {
            return ahora;
        }
    }

    private final Reloj reloj = new Reloj();
    private final Servidor servidor = Servidor.conDatosDemo(reloj);

    /** Cada paso abre su propia sesión (las sesiones caducan a los 30 min de inactividad, CU01). */
    private String hacer(String login, String accion, String... valores) {
        SesionCliente s = CatalogoAcciones.sesion(servidor);
        s.iniciar(login, Demo.CLAVE);
        try {
            Resultado r = s.ejecutar(Demo.accion(s, accion), List.of(valores));
            assertTrue(r.correcto(), login + " · " + accion + ": " + r.mensaje());
            return r.mensaje() + "\n" + String.join("\n", r.lineas());
        } finally {
            s.cerrar();
        }
    }

    private static String id(String texto, String prefijo) {
        Matcher m = Pattern.compile(prefijo + " (\\d+)").matcher(texto);
        assertTrue(m.find(), "No aparece «" + prefijo + " n» en: " + texto);
        return m.group(1);
    }

    private static void contiene(String texto, String esperado) {
        assertTrue(texto.contains(esperado), "Se esperaba «" + esperado + "» en:\n" + texto);
    }

    @Test
    @DisplayName("ItN: de la solicitud del médico a las estadísticas, pasando por cita, realización, informe y encuesta")
    void cicloDeVidaCompleto() {
        // CU04: el médico solicita una ecografía para PAC-1
        String solicitud = id(hacer("medico1", "Solicitar prueba", "PAC-1", "US-ABD", "Dolor abdominal",
                "Descartar colelitiasis", "ORDINARIO"), "Solicitud");
        // CU05 + CU11: admisión abre una agenda de ecografía mañana y cita la solicitud en el primer hueco
        hacer("admision1", "Crear agenda", "2026-11-03", "ECO1", "US", "medico1", "15:00", "16:00", "15");
        String citada = hacer("admision1", "Citar en el primer hueco", solicitud);
        contiene(citada, "2026-11-03T15:00");
        String cita = id(citada, "Cita");
        // CU07 + CU12: la paciente ve el aviso y confirma
        contiene(hacer("paciente1", "Mis notificaciones"), "CITA_ASIGNADA");
        contiene(hacer("paciente1", "Confirmar cita", cita), "CONFIRMADA");

        // CU08 + CU09: mañana a las 15:05 el médico registra la realización y redacta el informe
        reloj.fijar(LocalDateTime.of(2026, 11, 3, 15, 5));
        hacer("medico1", "Registrar realización", cita, "3", "Sin incidencias");
        hacer("medico1", "Crear informe", solicitud, "Vesícula sin litiasis", "Normal");

        // CU12 + CU13 + CU15: la paciente recibe el aviso, consulta su historial e informe y valora la prueba
        contiene(hacer("paciente1", "Mis notificaciones"), "INFORME_DISPONIBLE");
        contiene(hacer("paciente1", "Mi historial"), "solicitud " + solicitud + " · informe disponible");
        contiene(hacer("paciente1", "Ver informe de una prueba", solicitud), "Conclusión: Normal");
        contiene(hacer("paciente1", "Encuestas pendientes"), "Solicitud " + solicitud);
        hacer("paciente1", "Responder encuesta", solicitud, "5", "Muy bien atendida");
        hacer("paciente1", "Comunicar incidencia", "CITA", "La sala de espera estaba llena", solicitud);
        String incidencia = id(hacer("admision1", "Incidencias abiertas"), "Incidencia");
        hacer("admision1", "Responder incidencia", incidencia, "Gracias, lo revisamos");
        contiene(hacer("paciente1", "Mis incidencias"), "Respuesta: Gracias, lo revisamos");

        // CU19 + CU18: el administrador envía el informe a la HCE y comprueba la auditoría de la paciente
        contiene(hacer("admin1", "Enviar informes a la HCE"), "1 procesados");
        contiene(hacer("admin1", "Registros de un usuario", "paciente1", "2026-11-02", "2026-11-03"),
                "informe de la solicitud " + solicitud);

        // CU20 + CU21 + CU22: estadísticas del médico, informe operativo y estadísticas globales
        contiene(hacer("medico1", "Mis estadísticas", "2026-11-02", "2026-11-03"), "INFORMADA=1");
        contiene(hacer("admision1", "Informe operativo", "2026-11-03", "2026-11-03"), "US: 1/4 huecos");
        String globales = hacer("admin1", "Estadísticas globales", "2026-11-02", "2026-11-03");
        contiene(globales, "satisfacción media: 5.0/5");
        contiene(globales, "informes: 1");
    }
}
