package es.uclm.esi.iso2.servidor.solicitudes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.function.Consumer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import es.uclm.esi.iso2.servidor.catalogo.Prioridad;
import es.uclm.esi.iso2.servidor.comun.ReglaNegocioException;

class SolicitudPruebaTest {

    static final LocalDateTime FECHA = LocalDateTime.of(2026, 11, 2, 9, 0);

    static SolicitudPrueba nueva() {
        return new SolicitudPrueba(1, "medico1",
                new DatosSolicitud("PAC-1", "RX-T", "Tos", "Descartar neumonía", Prioridad.ORDINARIO), FECHA, null);
    }

    /** Lleva una solicitud nueva al estado pedido por el camino válido. */
    static SolicitudPrueba enEstado(EstadoSolicitud estado) {
        SolicitudPrueba s = nueva();
        switch (estado) {
            case SOLICITADA -> { }
            case CITADA -> s.citar();
            case REALIZADA -> { s.citar(); s.registrarRealizacion(FECHA, "ok"); }
            case INFORMADA -> { s.citar(); s.registrarRealizacion(FECHA, "ok"); s.informar(); }
            case CANCELADA -> s.cancelar("Ya no es necesaria");
        }
        return s;
    }

    static Consumer<SolicitudPrueba> operacion(String nombre) {
        return switch (nombre) {
            case "citar" -> SolicitudPrueba::citar;
            case "anularCita" -> SolicitudPrueba::anularCita;
            case "realizar" -> s -> s.registrarRealizacion(FECHA, null);
            case "informar" -> SolicitudPrueba::informar;
            case "cancelar" -> s -> s.cancelar("motivo");
            case "repriorizar" -> s -> s.cambiarPrioridad(Prioridad.URGENTE);
            default -> throw new IllegalArgumentException(nombre);
        };
    }

    @ParameterizedTest(name = "{0} + {1} → {2}")
    @CsvSource({
            "SOLICITADA,citar,CITADA", "SOLICITADA,cancelar,CANCELADA", "SOLICITADA,repriorizar,SOLICITADA",
            "SOLICITADA,anularCita,ERROR", "SOLICITADA,realizar,ERROR", "SOLICITADA,informar,ERROR",
            "CITADA,anularCita,SOLICITADA", "CITADA,realizar,REALIZADA",
            "CITADA,citar,ERROR", "CITADA,cancelar,ERROR", "CITADA,informar,ERROR", "CITADA,repriorizar,ERROR",
            "REALIZADA,informar,INFORMADA",
            "REALIZADA,citar,ERROR", "REALIZADA,cancelar,ERROR", "REALIZADA,realizar,ERROR",
            "INFORMADA,informar,ERROR", "INFORMADA,cancelar,ERROR", "INFORMADA,citar,ERROR",
            "CANCELADA,citar,ERROR", "CANCELADA,cancelar,ERROR", "CANCELADA,repriorizar,ERROR" })
    @DisplayName("Tabla de transiciones estado × operación (válidas e inválidas)")
    void transiciones(EstadoSolicitud inicial, String op, String esperado) {
        SolicitudPrueba s = enEstado(inicial);
        if (esperado.equals("ERROR")) {
            assertThrows(ReglaNegocioException.class, () -> operacion(op).accept(s));
            assertEquals(inicial, s.getEstado());
        } else {
            operacion(op).accept(s);
            assertEquals(EstadoSolicitud.valueOf(esperado), s.getEstado());
        }
    }

    @ParameterizedTest(name = "{0} abierta")
    @EnumSource(value = EstadoSolicitud.class, names = { "SOLICITADA", "CITADA" })
    void abiertas(EstadoSolicitud e) {
        assertTrue(e.abierta());
    }

    @ParameterizedTest(name = "{0} cerrada")
    @EnumSource(value = EstadoSolicitud.class, names = { "REALIZADA", "INFORMADA", "CANCELADA" })
    void cerradas(EstadoSolicitud e) {
        assertFalse(e.abierta());
    }

    @Test
    @DisplayName("Una solicitud nueva está SOLICITADA y guarda sus datos")
    void nuevaSolicitud() {
        SolicitudPrueba s = nueva();
        assertEquals(EstadoSolicitud.SOLICITADA, s.getEstado());
        assertEquals("PAC-1", s.getIdPaciente());
        assertEquals("medico1", s.getLoginMedico());
        assertEquals("Tos", s.getIndicacionClinica());
        assertEquals("Descartar neumonía", s.getJustificacion());
        assertEquals(FECHA, s.getFechaSolicitud());
        assertNull(s.getIdSeguimiento());
        assertNull(s.getFechaRealizacion());
        assertTrue(s.toString().contains("RX-T"));
    }

    @Test
    @DisplayName("Registrar realización guarda fecha y observaciones; cancelar guarda el motivo")
    void datosDeTransicion() {
        SolicitudPrueba s = enEstado(EstadoSolicitud.CITADA);
        s.registrarRealizacion(FECHA.plusDays(1), "  sin incidencias ");
        assertEquals(FECHA.plusDays(1), s.getFechaRealizacion());
        assertEquals("sin incidencias", s.getObservaciones());
        SolicitudPrueba c = nueva();
        c.cancelar("Duplicada");
        assertEquals("Duplicada", c.getMotivoCancelacion());
        SolicitudPrueba r = nueva();
        r.cambiarPrioridad(Prioridad.PREFERENTE);
        assertEquals(Prioridad.PREFERENTE, r.getPrioridad());
    }

    @Test
    @DisplayName("Conjetura de errores: campos vacíos, motivo vacío, fecha de realización nula")
    void errores() {
        assertThrows(IllegalArgumentException.class, () -> new SolicitudPrueba(1, "m",
                new DatosSolicitud("PAC-1", "RX", " ", "J", Prioridad.URGENTE), FECHA, null));
        assertThrows(IllegalArgumentException.class, () -> new SolicitudPrueba(1, "m",
                new DatosSolicitud("PAC-1", "RX", "I", "J", null), FECHA, null));
        assertThrows(IllegalArgumentException.class, () -> new SolicitudPrueba(1, "m", null, FECHA, null));
        assertThrows(IllegalArgumentException.class, () -> nueva().cancelar(""));
        assertThrows(IllegalArgumentException.class, () -> enEstado(EstadoSolicitud.CITADA).registrarRealizacion(null, ""));
        assertThrows(IllegalArgumentException.class, () -> nueva().cambiarPrioridad(null));
    }
}
