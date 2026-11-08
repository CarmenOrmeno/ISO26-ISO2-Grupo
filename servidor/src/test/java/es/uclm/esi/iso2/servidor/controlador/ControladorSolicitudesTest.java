package es.uclm.esi.iso2.servidor.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import es.uclm.esi.iso2.servidor.api.ApiSolicitudes;
import es.uclm.esi.iso2.servidor.catalogo.Prioridad;
import es.uclm.esi.iso2.servidor.comun.AccesoDenegadoException;
import es.uclm.esi.iso2.servidor.solicitudes.DatosSolicitud;
import es.uclm.esi.iso2.servidor.solicitudes.EstadoSolicitud;

class ControladorSolicitudesTest {

    private final Entorno e = new Entorno().conDatosBasicos();
    private final ApiSolicitudes api = e.servidor.solicitudes();

    @Test
    @DisplayName("CU04: el médico solicita una prueba y queda como solicitante")
    void solicitar() {
        long id = e.solicitar("medico1", "PAC-1", "RX-T", Prioridad.URGENTE);
        String t = e.token("medico1");
        assertEquals("medico1", api.obtener(t, id).getLoginMedico());
        assertEquals(Prioridad.URGENTE, api.obtener(t, id).getPrioridad());
        api.cancelar(t, id, "Ya no es necesaria");
        assertEquals(EstadoSolicitud.CANCELADA, api.obtener(e.token("admision1"), id).getEstado());
    }

    @Test
    @DisplayName("Otro médico no puede ver ni cancelar la solicitud")
    void otroMedico() {
        long id = e.solicitar("medico1", "PAC-1", "RX-T", Prioridad.ORDINARIO);
        String t = e.token("medico2");
        assertThrows(AccesoDenegadoException.class, () -> api.obtener(t, id));
        assertThrows(AccesoDenegadoException.class, () -> api.cancelar(t, id, "x"));
    }

    @ParameterizedTest(name = "{0} no puede solicitar pruebas")
    @ValueSource(strings = { "paciente1", "admision1", "admin1" })
    void sinPermiso(String login) {
        String t = e.token(login);
        assertThrows(AccesoDenegadoException.class, () -> api.solicitar(t,
                new DatosSolicitud("PAC-1", "RX-T", "I", "J", Prioridad.ORDINARIO)));
    }

    @ParameterizedTest(name = "{0} no puede consultar solicitudes")
    @ValueSource(strings = { "paciente1", "admin1" })
    void sinConsulta(String login) {
        long id = e.solicitar("medico1", "PAC-1", "RX-T", Prioridad.ORDINARIO);
        assertThrows(AccesoDenegadoException.class, () -> api.obtener(e.token(login), id));
    }
}
