package es.uclm.esi.iso2.servidor.comun;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RepositorioEnMemoriaTest {

    private record Elemento(String id, int valor) {
    }

    private Repositorio<Elemento, String> repo;

    @BeforeEach
    void preparar() {
        repo = new RepositorioEnMemoria<>(Elemento::id);
    }

    @Test
    @DisplayName("Guardar y buscar devuelve el mismo elemento")
    void guardarYBuscar() {
        Elemento e = repo.guardar(new Elemento("a", 1));
        assertEquals(e, repo.buscar("a").orElseThrow());
        assertTrue(repo.existe("a"));
    }

    @Test
    @DisplayName("Guardar con un id existente sustituye el elemento y conserva el orden")
    void guardarSustituye() {
        repo.guardar(new Elemento("a", 1));
        repo.guardar(new Elemento("b", 2));
        repo.guardar(new Elemento("a", 3));
        assertEquals(List.of(new Elemento("a", 3), new Elemento("b", 2)), repo.todos());
    }

    @Test
    @DisplayName("Obtener un id inexistente lanza NoEncontradoException")
    void obtenerInexistente() {
        assertThrows(NoEncontradoException.class, () -> repo.obtener("x"));
        assertTrue(repo.buscar("x").isEmpty());
    }

    @Test
    @DisplayName("Filtrar devuelve solo los que cumplen la condición")
    void filtrar() {
        repo.guardar(new Elemento("a", 1));
        repo.guardar(new Elemento("b", 5));
        assertEquals(List.of(new Elemento("b", 5)), repo.filtrar(e -> e.valor() > 2));
    }

    @Test
    @DisplayName("Eliminar quita el elemento; eliminar dos veces lanza NoEncontradoException")
    void eliminar() {
        repo.guardar(new Elemento("a", 1));
        repo.eliminar("a");
        assertFalse(repo.existe("a"));
        assertThrows(NoEncontradoException.class, () -> repo.eliminar("a"));
    }

    @Test
    @DisplayName("Guardar null lanza NullPointerException")
    void guardarNulo() {
        assertThrows(NullPointerException.class, () -> repo.guardar(null));
    }
}
