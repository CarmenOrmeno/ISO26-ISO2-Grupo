package es.uclm.esi.iso2.servidor.integracion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SsoSimuladoTest {

    private final SsoSimulado sso = new SsoSimulado().registrar("ana", "clave", "Ana Ruiz");

    @Test
    @DisplayName("Credenciales correctas devuelven la identidad")
    void credencialesCorrectas() {
        assertEquals(new IdentidadSso("ana", "Ana Ruiz"), sso.autenticar("ana", "clave").orElseThrow());
    }

    @ParameterizedTest(name = "login={0}, contraseña={1} → rechazado")
    @CsvSource(nullValues = "NULL", value = { "ana,mala", "pepe,clave", "NULL,clave", "ana,NULL", "ANA,clave" })
    @DisplayName("Credenciales incorrectas o nulas no autentican")
    void credencialesIncorrectas(String login, String contrasena) {
        assertTrue(sso.autenticar(login, contrasena).isEmpty());
    }
}
