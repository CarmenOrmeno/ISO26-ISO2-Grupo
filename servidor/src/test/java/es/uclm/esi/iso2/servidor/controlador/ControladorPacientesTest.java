package es.uclm.esi.iso2.servidor.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import es.uclm.esi.iso2.servidor.api.ApiPacientes;
import es.uclm.esi.iso2.servidor.comun.AccesoDenegadoException;
import es.uclm.esi.iso2.servidor.comun.NoEncontradoException;
import es.uclm.esi.iso2.servidor.pacientes.DatosPaciente;

class ControladorPacientesTest {

    private final Entorno e = new Entorno();
    private final ApiPacientes api = e.servidor.pacientes();
    private final DatosPaciente datos = new DatosPaciente("12345678Z", "Lucía", "Martín",
            LocalDate.of(1980, 5, 14), "600111222", null);

    @Test
    @DisplayName("CU02: el administrativo da de alta, modifica, consulta y da de baja")
    void flujoAdministrativo() {
        String t = e.token("admision1");
        String id = api.alta(t, datos).getId();
        api.modificar(t, id, new DatosPaciente("12345678Z", "Lucía", "Martín Pérez",
                LocalDate.of(1980, 5, 14), "600111222", null));
        assertEquals("Martín Pérez", api.obtener(t, id).getApellidos());
        assertEquals(id, api.buscarPorDni(t, "12345678Z").getId());
        assertEquals(1, api.listarActivos(t).size());
        api.baja(t, id);
        assertFalse(api.obtener(t, id).isActivo());
        assertThrows(NoEncontradoException.class, () -> api.buscarPorDni(t, "87654321X"));
    }

    @Test
    @DisplayName("El médico puede consultar pacientes pero no darlos de alta")
    void medico() {
        String id = api.alta(e.token("admision1"), datos).getId();
        String t = e.token("medico1");
        assertEquals("Lucía", api.obtener(t, id).getNombre());
        assertThrows(AccesoDenegadoException.class, () -> api.alta(t, datos));
        assertThrows(AccesoDenegadoException.class, () -> api.baja(t, id));
        assertThrows(AccesoDenegadoException.class, () -> api.modificar(t, id, datos));
    }

    @ParameterizedTest(name = "{0} no puede consultar ni gestionar pacientes")
    @ValueSource(strings = { "paciente1", "admin1" })
    void sinPermiso(String login) {
        String t = e.token(login);
        assertThrows(AccesoDenegadoException.class, () -> api.listarActivos(t));
        assertThrows(AccesoDenegadoException.class, () -> api.buscarPorDni(t, "12345678Z"));
        assertThrows(AccesoDenegadoException.class, () -> api.alta(t, datos));
    }
}
