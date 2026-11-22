package es.uclm.esi.iso2.cliente.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Ejecución de una acción y cómo se presenta su resultado. */
class AccionTest {

    @Test
    @DisplayName("Una lista se muestra como líneas con el número de resultados")
    void lista() {
        Resultado r = Accion.de("G", "Lista", (t, v) -> List.of("a", "b")).ejecutar("t", List.of());
        assertTrue(r.correcto());
        assertEquals("2 resultados", r.mensaje());
        assertEquals(List.of("a", "b"), r.lineas());
        assertEquals("1 resultado", Accion.de("G", "L", (t, v) -> List.of(1)).ejecutar("t", List.of()).mensaje());
        assertEquals("Sin resultados", Accion.de("G", "L", (t, v) -> List.of()).ejecutar("t", List.of()).mensaje());
    }

    @Test
    @DisplayName("Un objeto se muestra por líneas; un texto, con su primera línea como mensaje; nulo es «Hecho»")
    void objetoYTexto() {
        Object objeto = new Object() {
            @Override
            public String toString() {
                return "uno\ndos";
            }
        };
        assertEquals(List.of("uno", "dos"), Resultado.de(objeto).lineas());
        Resultado texto = Resultado.de("Título\nlínea");
        assertEquals("Título", texto.mensaje());
        assertEquals(List.of("línea"), texto.lineas());
        assertEquals("Hecho", Resultado.de(null).mensaje());
    }

    @Test
    @DisplayName("Una orden sin resultado muestra su mensaje y recibe el token y los valores")
    void orden() {
        AtomicBoolean llamada = new AtomicBoolean();
        Accion a = Accion.orden("G", "Orden", "Hecho bien", (t, v) -> llamada.set(t.equals("tk") && v.largo(0) == 7),
                Campo.id("Id"));
        assertEquals("Hecho bien", a.ejecutar("tk", List.of("7")).mensaje());
        assertTrue(llamada.get());
        assertEquals("Orden", a.toString());
    }

    @Test
    @DisplayName("Un valor mal escrito no llega al servidor; un error del servidor se devuelve como resultado")
    void errores() {
        AtomicBoolean llamada = new AtomicBoolean();
        Accion a = Accion.de("G", "A", (t, v) -> {
            llamada.set(true);
            return null;
        }, Campo.fecha("Fecha"));
        Resultado mal = a.ejecutar("t", List.of("ayer"));
        assertFalse(mal.correcto());
        assertFalse(llamada.get());
        Resultado servidor = Accion.de("G", "B", (t, v) -> {
            throw new IllegalStateException("fallo del servidor");
        }).ejecutar("t", List.of());
        assertEquals("fallo del servidor", servidor.mensaje());
        assertEquals("NullPointerException",
                Accion.de("G", "C", (t, v) -> {
                    throw new NullPointerException();
                }).ejecutar("t", List.of()).mensaje());
    }
}
