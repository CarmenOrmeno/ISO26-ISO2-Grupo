package es.uclm.esi.iso2.servidor.atencion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.uclm.esi.iso2.servidor.comun.ReglaNegocioException;

/** CU15 · Invariantes y ciclo de vida de la incidencia del paciente. */
class IncidenciaPacienteTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 3, 10, 0);

    private static IncidenciaPaciente incidencia(String descripcion) {
        return new IncidenciaPaciente(1, "PAC-1", CategoriaIncidencia.CITA, descripcion, null, AHORA);
    }

    @Test
    @DisplayName("Una incidencia nueva está abierta y sin respuesta")
    void nueva() {
        IncidenciaPaciente i = incidencia("  La sala de espera estaba llena ");
        assertEquals(EstadoIncidencia.ABIERTA, i.getEstado());
        assertEquals("La sala de espera estaba llena", i.getDescripcion());
        assertEquals("", i.getRespuesta());
        assertNull(i.getFechaRespuesta());
        assertNull(i.getIdSolicitud());
    }

    @Test
    @DisplayName("Valores límite de la descripción: 1000 caracteres sí, 1001 no; vacía o nula no")
    void longitudDescripcion() {
        assertEquals(1000, incidencia("a".repeat(1000)).getDescripcion().length());
        assertThrows(IllegalArgumentException.class, () -> incidencia("a".repeat(1001)));
        assertThrows(IllegalArgumentException.class, () -> incidencia("   "));
        assertThrows(IllegalArgumentException.class, () -> incidencia(null));
    }

    @Test
    @DisplayName("La categoría es obligatoria")
    void categoriaObligatoria() {
        assertThrows(IllegalArgumentException.class,
                () -> new IncidenciaPaciente(1, "PAC-1", null, "Texto", null, AHORA));
    }

    @Test
    @DisplayName("ABIERTA → RESPONDIDA guarda respuesta, autor y fecha; no se responde dos veces")
    void responder() {
        IncidenciaPaciente i = incidencia("Me cambiaron la cita sin avisar");
        i.responder("Disculpe, revisamos el aviso", "admision1", AHORA.plusHours(1));
        assertEquals(EstadoIncidencia.RESPONDIDA, i.getEstado());
        assertEquals("admision1", i.getRespondidaPor());
        assertEquals(AHORA.plusHours(1), i.getFechaRespuesta());
        assertTrue(i.toString().contains("Respuesta: Disculpe"));
        assertThrows(ReglaNegocioException.class, () -> i.responder("Otra", "admision1", AHORA));
    }

    @Test
    @DisplayName("Una respuesta vacía o de más de 1000 caracteres no cambia el estado")
    void respuestaInvalida() {
        IncidenciaPaciente i = incidencia("Texto");
        assertThrows(IllegalArgumentException.class, () -> i.responder(" ", "admision1", AHORA));
        assertThrows(IllegalArgumentException.class, () -> i.responder("a".repeat(1001), "admision1", AHORA));
        assertEquals(EstadoIncidencia.ABIERTA, i.getEstado());
    }
}
