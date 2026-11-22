package es.uclm.esi.iso2.servidor.comun;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.uclm.esi.iso2.servidor.persistencia.RepositorioEnMemoria;

/** D-018 · Los ids continúan desde el mayor guardado y solo se consumen al confirmar. */
class SecuenciaTest {

    private final Repositorio<Long, Long> repositorio = new RepositorioEnMemoria<>(n -> n);

    @Test
    @DisplayName("Repositorio vacío: el primer id es 1")
    void vacio() {
        assertEquals(1, Secuencia.continuando(repositorio, Long::longValue).siguiente());
    }

    @Test
    @DisplayName("Continúa desde el mayor id guardado aunque no estén en orden")
    void continua() {
        repositorio.guardar(7L);
        repositorio.guardar(3L);
        assertEquals(8, Secuencia.continuando(repositorio, Long::longValue).siguiente());
    }

    @Test
    @DisplayName("El siguiente no se consume hasta confirmar")
    void confirmar() {
        Secuencia s = Secuencia.continuando(repositorio, Long::longValue);
        assertEquals(1, s.siguiente());
        assertEquals(1, s.siguiente());
        s.confirmar();
        assertEquals(2, s.siguiente());
    }
}
