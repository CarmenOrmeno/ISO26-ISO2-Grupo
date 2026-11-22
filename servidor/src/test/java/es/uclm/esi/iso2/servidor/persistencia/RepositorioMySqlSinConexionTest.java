package es.uclm.esi.iso2.servidor.persistencia;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.SQLException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** D-018 · Errores que no necesitan base de datos: nombre de tabla y fallo de conexión. */
class RepositorioMySqlSinConexionTest {

    private static final RepositorioMySql.Conexiones CAIDA = () -> {
        throw new SQLException("MySQL no responde");
    };

    @ParameterizedTest(name = "nombre de tabla no válido: «{0}»")
    @ValueSource(strings = { "", "Pacientes", "1tabla", "tabla; DROP TABLE x", "con-guion" })
    void nombreInvalido(String nombre) {
        assertThrows(IllegalArgumentException.class, () -> new RepositorioMySql<String, String>(nombre, s -> s, CAIDA));
    }

    @Test
    @DisplayName("Sin conexión con MySQL se lanza PersistenciaException (error técnico, no de negocio)")
    void sinConexion() {
        assertThrows(PersistenciaException.class, () -> new RepositorioMySql<String, String>("pruebas", s -> s, CAIDA));
    }
}
