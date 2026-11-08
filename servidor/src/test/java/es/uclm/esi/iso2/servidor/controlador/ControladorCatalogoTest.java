package es.uclm.esi.iso2.servidor.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import es.uclm.esi.iso2.servidor.api.ApiCatalogo;
import es.uclm.esi.iso2.servidor.catalogo.Modalidad;
import es.uclm.esi.iso2.servidor.catalogo.Prioridad;
import es.uclm.esi.iso2.servidor.catalogo.TipoPrueba;
import es.uclm.esi.iso2.servidor.comun.AccesoDenegadoException;

class ControladorCatalogoTest {

    private final Entorno e = new Entorno();
    private final ApiCatalogo api = e.servidor.catalogo();
    private final TipoPrueba rx = new TipoPrueba("RX-T", "Rx tórax", Modalidad.RX, "RX1", 10, "", false, false, "", false);

    @Test
    @DisplayName("CU03: el administrador configura catálogo y parámetros")
    void administrador() {
        String t = e.token("admin1");
        api.alta(t, rx);
        api.modificar(t, new TipoPrueba("RX-T", "Rx tórax PA", Modalidad.RX, "RX1", 10, "", false, false, "", false));
        assertEquals("Rx tórax PA", api.obtener(t, "RX-T").nombre());
        assertEquals(1, api.listarPorModalidad(t, Modalidad.RX).size());
        api.cambiarHorario(t, LocalTime.of(8, 30), LocalTime.of(20, 0));
        api.cambiarPlazo(t, Prioridad.PREFERENTE, 10);
        api.cambiarHorasRecordatorio(t, 48);
        assertTrue(api.parametros(t).contains("08:30-20:00"));
        api.baja(t, "RX-T");
        assertTrue(api.listar(t).isEmpty());
    }

    @ParameterizedTest(name = "{0} consulta el catálogo pero no lo configura")
    @ValueSource(strings = { "paciente1", "medico1", "admision1" })
    void soloConsulta(String login) {
        api.alta(e.token("admin1"), rx);
        String t = e.token(login);
        assertEquals(1, api.listar(t).size());
        assertThrows(AccesoDenegadoException.class, () -> api.alta(t, rx));
        assertThrows(AccesoDenegadoException.class, () -> api.modificar(t, rx));
        assertThrows(AccesoDenegadoException.class, () -> api.baja(t, "RX-T"));
        assertThrows(AccesoDenegadoException.class, () -> api.cambiarPlazo(t, Prioridad.URGENTE, 1));
        assertThrows(AccesoDenegadoException.class, () -> api.cambiarHorario(t, LocalTime.of(8, 0), LocalTime.of(9, 0)));
        assertThrows(AccesoDenegadoException.class, () -> api.cambiarHorasRecordatorio(t, 2));
    }
}
