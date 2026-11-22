package es.uclm.esi.iso2.cliente;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.uclm.esi.iso2.servidor.persistencia.PersistenciaException;

/** ItN · Arranque del cliente cuando la base de datos configurada no responde. */
class MainTest {

    @Test
    @DisplayName("Sin MySQL: mensaje claro con la causa y cómo seguir, sin volcado de pila")
    void mensajeSinBaseDeDatos() {
        String m = Main.mensajeSinBaseDeDatos(new PersistenciaException("No se pudo preparar la tabla usuarios",
                new SQLException("Communications link failure\n\nThe last packet...")));
        assertTrue(m.startsWith("No se puede arrancar: No se pudo preparar la tabla usuarios (Communications link failure)."));
        assertTrue(m.contains("quita RADIO_DB_URL"));
    }
}
