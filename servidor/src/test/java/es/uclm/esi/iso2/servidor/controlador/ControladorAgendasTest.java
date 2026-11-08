package es.uclm.esi.iso2.servidor.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import es.uclm.esi.iso2.servidor.agendas.DatosAgenda;
import es.uclm.esi.iso2.servidor.api.ApiAgendas;
import es.uclm.esi.iso2.servidor.catalogo.Modalidad;
import es.uclm.esi.iso2.servidor.comun.AccesoDenegadoException;

class ControladorAgendasTest {

    private final Entorno e = new Entorno();
    private final ApiAgendas api = e.servidor.agendas();
    private final LocalDate manana = LocalDate.of(2026, 11, 3);
    private final DatosAgenda datos = new DatosAgenda(manana, "RX1", Modalidad.RX, "medico1",
            LocalTime.of(9, 0), LocalTime.of(10, 0), 15);

    @Test
    @DisplayName("CU05: admisión crea, consulta y elimina agendas")
    void flujo() {
        String t = e.token("admision1");
        long id = api.crear(t, datos).getId();
        assertEquals(4, api.obtener(t, id).getHuecos().size());
        assertEquals(1, api.delDia(t, manana).size());
        assertEquals(4, api.huecosLibres(t, Modalidad.RX, manana, manana).size());
        api.eliminar(t, id);
        assertEquals(0, api.delDia(t, manana).size());
    }

    @ParameterizedTest(name = "{0} no gestiona agendas")
    @ValueSource(strings = { "paciente1", "medico1", "admin1" })
    void sinPermiso(String login) {
        String t = e.token(login);
        assertThrows(AccesoDenegadoException.class, () -> api.crear(t, datos));
        assertThrows(AccesoDenegadoException.class, () -> api.eliminar(t, 1));
    }

    @ParameterizedTest(name = "{0} no consulta agendas (todavía)")
    @ValueSource(strings = { "paciente1", "admin1" })
    void sinConsulta(String login) {
        String t = e.token(login);
        assertThrows(AccesoDenegadoException.class, () -> api.delDia(t, manana));
        assertThrows(AccesoDenegadoException.class, () -> api.obtener(t, 1));
        assertThrows(AccesoDenegadoException.class, () -> api.huecosLibres(t, Modalidad.RX, manana, manana));
    }
}
