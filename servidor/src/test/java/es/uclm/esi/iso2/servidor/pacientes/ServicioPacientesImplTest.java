package es.uclm.esi.iso2.servidor.pacientes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.uclm.esi.iso2.servidor.comun.NoEncontradoException;
import es.uclm.esi.iso2.servidor.comun.RelojAjustable;
import es.uclm.esi.iso2.servidor.comun.RepositorioEnMemoria;
import es.uclm.esi.iso2.servidor.comun.ReglaNegocioException;

class ServicioPacientesImplTest {

    private ServicioPacientes servicio;

    @BeforeEach
    void preparar() {
        servicio = new ServicioPacientesImpl(new RepositorioEnMemoria<>(Paciente::getId),
                new RelojAjustable(LocalDateTime.of(2026, 11, 2, 9, 0)));
    }

    private static DatosPaciente datos(String dni) {
        return new DatosPaciente(dni, "Nombre", "Apellidos", LocalDate.of(1970, 1, 1), "600000000", null);
    }

    @Test
    @DisplayName("Las altas reciben ids correlativos PAC-1, PAC-2…")
    void idsCorrelativos() {
        assertEquals("PAC-1", servicio.alta(datos("12345678Z")).getId());
        assertEquals("PAC-2", servicio.alta(datos("87654321X")).getId());
    }

    @Test
    @DisplayName("DNI repetido (aunque en minúsculas) se rechaza y no consume id")
    void dniRepetido() {
        servicio.alta(datos("12345678Z"));
        assertThrows(ReglaNegocioException.class, () -> servicio.alta(datos("12345678z")));
        assertEquals("PAC-2", servicio.alta(datos("87654321X")).getId());
    }

    @Test
    @DisplayName("Datos inválidos no dan de alta ni consumen id")
    void datosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> servicio.alta(datos("12345678A")));
        assertEquals("PAC-1", servicio.alta(datos("12345678Z")).getId());
    }

    @Test
    @DisplayName("Modificar, buscar por DNI, baja y listado de activos")
    void cicloCompleto() {
        Paciente p = servicio.alta(datos("12345678Z"));
        servicio.alta(datos("87654321X"));
        servicio.modificar(p.getId(), new DatosPaciente("12345678Z", "Otro", "Nombre",
                LocalDate.of(1970, 1, 1), "600000001", null));
        assertEquals("Otro", servicio.buscarPorDni("12345678Z").orElseThrow().getNombre());
        servicio.baja(p.getId());
        assertEquals(List.of("PAC-2"), servicio.activos().stream().map(Paciente::getId).toList());
        assertTrue(servicio.buscarPorDni("11111111H").isEmpty());
    }

    @Test
    @DisplayName("obtenerActivo: inexistente o dado de baja → ReglaNegocioException")
    void obtenerActivo() {
        Paciente p = servicio.alta(datos("12345678Z"));
        assertEquals(p, servicio.obtenerActivo("PAC-1"));
        assertThrows(ReglaNegocioException.class, () -> servicio.obtenerActivo("PAC-9"));
        servicio.baja("PAC-1");
        assertThrows(ReglaNegocioException.class, () -> servicio.obtenerActivo("PAC-1"));
    }

    @Test
    @DisplayName("Operar sobre un id inexistente → NoEncontradoException")
    void inexistente() {
        assertThrows(NoEncontradoException.class, () -> servicio.obtener("PAC-9"));
        assertThrows(NoEncontradoException.class, () -> servicio.baja("PAC-9"));
        assertThrows(NoEncontradoException.class, () -> servicio.modificar("PAC-9", datos("12345678Z")));
    }
}
