package es.uclm.esi.iso2.servidor.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** D-018 · Configuración de la conexión a MySQL. */
class ConfiguracionBdTest {

    private static final String URL = "jdbc:mysql://127.0.0.1:3306/bd";

    @Test
    @DisplayName("Sin URL en el entorno no hay configuración (se usa memoria)")
    void sinUrl() {
        assertTrue(ConfiguracionBd.desde("RADIO_DB_URL", Map.<String, String>of()::get).isEmpty());
        assertTrue(ConfiguracionBd.desde("RADIO_DB_URL", Map.of("RADIO_DB_URL", " ")::get).isEmpty());
    }

    @Test
    @DisplayName("Con URL, usuario y clave se construye la configuración")
    void completa() {
        ConfiguracionBd c = ConfiguracionBd.desde("RADIO_DB_URL",
                Map.of("RADIO_DB_URL", URL, "RADIO_DB_USUARIO", "app", "RADIO_DB_CLAVE", "secreta")::get).orElseThrow();
        assertEquals(URL, c.url());
        assertEquals("app", c.usuario());
        assertEquals("secreta", c.clave());
    }

    @Test
    @DisplayName("La contraseña nunca aparece al mostrar la configuración")
    void ocultaClave() {
        ConfiguracionBd c = new ConfiguracionBd(URL, "app", "secreta");
        assertFalse(c.toString().contains("secreta"));
        assertFalse(new FabricaMySql(c).descripcion().contains("secreta"));
        assertEquals("", new ConfiguracionBd(URL, "app", null).clave());
    }

    @Test
    @DisplayName("URL que no es de MySQL o usuario vacío no son válidos")
    void invalida() {
        assertThrows(IllegalArgumentException.class, () -> new ConfiguracionBd("jdbc:h2:mem:x", "app", "x"));
        assertThrows(IllegalArgumentException.class, () -> new ConfiguracionBd(URL, " ", "x"));
    }

    @Test
    @DisplayName("La fábrica en memoria se describe como no persistente")
    void memoria() {
        assertTrue(new FabricaEnMemoria().descripcion().startsWith("memoria"));
    }
}
