package es.uclm.esi.iso2.servidor.seguridad;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SesionTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 11, 2, 9, 0);

    @ParameterizedTest(name = "{0} s de inactividad → caducada={1}")
    @CsvSource({ "0,false", "1799,false", "1800,true", "1801,true" })
    @DisplayName("Valores límite de la caducidad por inactividad (30 min)")
    void caducidad(long segundos, boolean caducada) {
        Sesion s = new Sesion("t", "u", INICIO);
        assertEquals(caducada, s.caducada(INICIO.plusSeconds(segundos), Duration.ofMinutes(30)));
    }

    @Test
    @DisplayName("Registrar un acceso renueva la sesión")
    void registrarAccesoRenueva() {
        Sesion s = new Sesion("t", "u", INICIO);
        s.registrarAcceso(INICIO.plusMinutes(20));
        assertEquals(false, s.caducada(INICIO.plusMinutes(40), Duration.ofMinutes(30)));
        assertEquals(INICIO, s.getInicio());
        assertEquals(INICIO.plusMinutes(20), s.getUltimoAcceso());
        assertEquals("u", s.getLogin());
        assertEquals("t", s.getToken());
    }
}
