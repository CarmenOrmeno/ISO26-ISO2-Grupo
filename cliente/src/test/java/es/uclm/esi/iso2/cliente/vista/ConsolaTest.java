package es.uclm.esi.iso2.cliente.vista;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConsolaTest {

    @Test
    @DisplayName("leer devuelve la línea sin espacios y lanza FinDeEntrada al acabar")
    void leer() {
        Guion g = new Guion("  hola  ");
        assertEquals("hola", g.consola().leer("Texto"));
        assertThrows(FinDeEntradaException.class, () -> g.consola().leer("Texto"));
    }

    @Test
    @DisplayName("leerEntero repite hasta recibir un número")
    void leerEntero() {
        Guion g = new Guion("abc", "", "42");
        assertEquals(42, g.consola().leerEntero("N"));
        assertTrue(g.salida().contains("«abc» no es un número"));
        assertEquals(7L, new Guion("7").consola().leerLargo("N"));
    }

    @Test
    @DisplayName("leerFecha y leerHora repiten hasta recibir un valor válido")
    void fechaYHora() {
        Guion g = new Guion("31/12/2026", "2026-12-31", "25:00", "09:30");
        assertEquals(LocalDate.of(2026, 12, 31), g.consola().leerFecha("Fecha"));
        assertEquals(LocalTime.of(9, 30), g.consola().leerHora("Hora"));
        assertTrue(g.salida().contains("no es una fecha válida"));
        assertTrue(g.salida().contains("no es una hora válida"));
    }

    @Test
    @DisplayName("Valores límite del menú de 2 opciones: -1 y 3 se rechazan; 0 y 2 se aceptan")
    void elegir() {
        Guion g = new Guion("-1", "3", "2", "0");
        assertEquals(2, g.consola().elegir("Menú", List.of("A", "B")));
        assertEquals(0, g.consola().elegir("Menú", List.of("A", "B")));
        assertTrue(g.salida().contains("Opción fuera de rango"));
    }

    @Test
    void confirmarYListar() {
        Guion g = new Guion("S", "n");
        assertTrue(g.consola().confirmar("¿Seguro?"));
        assertFalse(g.consola().confirmar("¿Seguro?"));
        g.consola().listar("Vacía", List.of());
        g.consola().listar("Llena", List.of("uno"));
        assertTrue(g.salida().contains("(sin resultados)"));
        assertTrue(g.salida().contains("- uno"));
    }
}
