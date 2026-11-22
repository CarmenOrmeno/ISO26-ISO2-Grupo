package es.uclm.esi.iso2.servidor.atencion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** CU15 · Invariantes de la encuesta de satisfacción. */
class EncuestaTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 3, 10, 0);

    private static Encuesta encuesta(int puntuacion, String comentario) {
        return new Encuesta(1, "PAC-1", puntuacion, comentario, AHORA);
    }

    @ParameterizedTest(name = "puntuación válida {0}")
    @ValueSource(ints = { 1, 3, 5 })
    void puntuacionValida(int puntuacion) {
        assertEquals(puntuacion, encuesta(puntuacion, "").puntuacion());
    }

    @ParameterizedTest(name = "puntuación inválida {0}")
    @ValueSource(ints = { 0, 6, -1 })
    void puntuacionInvalida(int puntuacion) {
        assertThrows(IllegalArgumentException.class, () -> encuesta(puntuacion, ""));
    }

    @Test
    @DisplayName("El comentario es opcional: nulo se guarda vacío y se quitan los espacios")
    void comentarioOpcional() {
        assertEquals("", encuesta(4, null).comentario());
        assertEquals("Muy amables", encuesta(4, "  Muy amables ").comentario());
    }

    @Test
    @DisplayName("Valores límite del comentario: 500 caracteres sí, 501 no")
    void longitudComentario() {
        assertEquals(500, encuesta(4, "a".repeat(500)).comentario().length());
        assertThrows(IllegalArgumentException.class, () -> encuesta(4, "a".repeat(501)));
    }

    @Test
    @DisplayName("Paciente y fecha son obligatorios")
    void obligatorios() {
        assertThrows(IllegalArgumentException.class, () -> new Encuesta(1, " ", 3, "", AHORA));
        assertThrows(IllegalArgumentException.class, () -> new Encuesta(1, "PAC-1", 3, "", null));
    }
}
