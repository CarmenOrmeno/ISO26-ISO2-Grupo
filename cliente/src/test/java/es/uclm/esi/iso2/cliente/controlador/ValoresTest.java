package es.uclm.esi.iso2.cliente.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import es.uclm.esi.iso2.servidor.catalogo.Prioridad;

/** Interpretación de lo que escribe el usuario en los campos de una acción (CEV/CEI de cada tipo). */
class ValoresTest {

    private static final List<Campo> CAMPOS = List.of(Campo.texto("Nombre"), Campo.textoOpcional("Nota"),
            Campo.fecha("Fecha"), Campo.hora("Hora"), Campo.entero("Número"), Campo.id("Id"),
            Campo.idOpcional("Prueba"), Campo.opcion("Prioridad", Prioridad.values()), Campo.siNo("Contraste"));

    @Test
    @DisplayName("Valores válidos de cada tipo; los opcionales vacíos son nulos")
    void validos() {
        Valores v = Valores.de(CAMPOS, List.of(" Ana ", "", "2026-11-02", "09:30", "5", "12", "", "urgente", "s"));
        assertEquals("Ana", v.texto(0));
        assertNull(v.texto(1));
        assertEquals("", v.textoOVacio(1));
        assertEquals(LocalDate.of(2026, 11, 2), v.fecha(2));
        assertEquals(LocalTime.of(9, 30), v.hora(3));
        assertEquals(5, v.entero(4));
        assertEquals(12L, v.largo(5));
        assertNull(v.largoOpcional(6));
        assertEquals(Prioridad.URGENTE, v.opcion(7, Prioridad.class));
        assertTrue(v.siNo(8));
    }

    @Test
    @DisplayName("Sí/no admite SI, NO, s y n")
    void siNo() {
        List<Campo> c = List.of(Campo.siNo("X"));
        assertFalse(Valores.de(c, List.of("n")).siNo(0));
        assertFalse(Valores.de(c, List.of("NO")).siNo(0));
        assertThrows(IllegalArgumentException.class, () -> Valores.de(c, List.of("quizá")));
    }

    @ParameterizedTest(name = "«{1}» en el campo {0} → error que nombra el campo")
    @CsvSource({ "2, 02/11/2026, Fecha", "3, 9h, Hora", "4, cinco, Número", "5, 1.5, Id", "7, MUY_URGENTE, Prioridad" })
    void invalidos(int indice, String texto, String campo) {
        List<String> textos = new java.util.ArrayList<>(
                List.of("Ana", "", "2026-11-02", "09:30", "5", "12", "", "URGENTE", "SI"));
        textos.set(indice, texto);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> Valores.de(CAMPOS, textos));
        assertTrue(e.getMessage().contains("«" + campo + "»"), e.getMessage());
    }

    @Test
    @DisplayName("Un campo obligatorio vacío o en blanco no vale; el número de valores debe coincidir")
    void obligatorioYTamano() {
        assertThrows(IllegalArgumentException.class, () -> Valores.de(List.of(Campo.texto("Nombre")), List.of("  ")));
        assertThrows(IllegalArgumentException.class, () -> Valores.de(List.of(Campo.fecha("F")), java.util.Arrays.asList((String) null)));
        assertThrows(IllegalArgumentException.class, () -> Valores.de(List.of(Campo.texto("Nombre")), List.of()));
    }

    @Test
    @DisplayName("La etiqueta lleva la pista de formato o las opciones")
    void etiquetas() {
        assertEquals("Fecha (AAAA-MM-DD)", Campo.fecha("Fecha").etiquetaConAyuda());
        assertEquals("Prioridad (URGENTE, PREFERENTE, ORDINARIO)",
                Campo.opcion("Prioridad", Prioridad.values()).etiquetaConAyuda());
    }

    @Test
    @DisplayName("Leer un campo con otro tipo o un índice que no existe es un error de programación")
    void errorDeProgramacion() {
        Valores v = Valores.de(List.of(Campo.fecha("Fecha")), List.of("2026-11-02"));
        IllegalStateException tipo = assertThrows(IllegalStateException.class, () -> v.largo(0));
        assertTrue(tipo.getMessage().startsWith(Valores.ERROR_DE_PROGRAMACION));
        assertTrue(tipo.getMessage().contains("LocalDate, no Long"));
        assertThrows(IllegalStateException.class, () -> v.texto(1));
        assertThrows(IllegalStateException.class, () -> v.texto(-1));
    }
}
