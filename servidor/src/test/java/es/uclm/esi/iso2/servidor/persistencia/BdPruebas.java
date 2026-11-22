package es.uclm.esi.iso2.servidor.persistencia;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Base de datos MySQL de pruebas (RADIO_DB_TEST_URL, RADIO_DB_USUARIO, RADIO_DB_CLAVE). En el CI siempre
 * está (servicio MySQL del workflow); en un equipo sin MySQL estas pruebas se omiten.
 */
public final class BdPruebas {

    private static final Optional<ConfiguracionBd> CONFIGURACION = ConfiguracionBd.desdeEntorno("RADIO_DB_TEST_URL");

    private BdPruebas() {
    }

    /** Configuración de la BD de pruebas vacía (se borran todas sus tablas). Omite la prueba si no hay BD. */
    public static ConfiguracionBd vacia() {
        assumeTrue(CONFIGURACION.isPresent(), "Sin RADIO_DB_TEST_URL: se omiten las pruebas contra MySQL");
        ConfiguracionBd c = CONFIGURACION.get();
        try (Connection con = DriverManager.getConnection(c.url(), c.usuario(), c.clave());
                Statement st = con.createStatement()) {
            List<String> tablas = new ArrayList<>();
            try (ResultSet rs = st.executeQuery(
                    "SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE()")) {
                while (rs.next()) {
                    tablas.add(rs.getString(1));
                }
            }
            for (String tabla : tablas) {
                st.execute("DROP TABLE `" + tabla + "`");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo vaciar la BD de pruebas", e);
        }
        return c;
    }
}
