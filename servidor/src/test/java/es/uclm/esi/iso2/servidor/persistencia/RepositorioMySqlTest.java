package es.uclm.esi.iso2.servidor.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.uclm.esi.iso2.servidor.comun.NoEncontradoException;
import es.uclm.esi.iso2.servidor.comun.Repositorio;
import es.uclm.esi.iso2.servidor.pacientes.DatosPaciente;
import es.uclm.esi.iso2.servidor.pacientes.Paciente;

/** D-018 · Integración: el repositorio MySQL cumple el contrato de {@link Repositorio} (LSP). */
class RepositorioMySqlTest {

    private FabricaMySql fabrica;
    private Repositorio<Paciente, String> pacientes;

    private static Paciente paciente(String id, String dni) {
        return new Paciente(id, new DatosPaciente(dni, "Lucía", "Martín", LocalDate.of(1980, 5, 14), "600111222",
                null), LocalDate.of(2026, 11, 2));
    }

    @BeforeEach
    void preparar() {
        fabrica = new FabricaMySql(BdPruebas.vacia());
        pacientes = fabrica.crear("pacientes", Paciente::getId);
    }

    @Test
    @DisplayName("Guardar y buscar devuelve una entidad igual, también desde otro repositorio (otro arranque)")
    void guardarYBuscar() {
        pacientes.guardar(paciente("PAC-1", "12345678Z"));
        Repositorio<Paciente, String> otroArranque = fabrica.crear("pacientes", Paciente::getId);
        Paciente leido = otroArranque.obtener("PAC-1");
        assertEquals("12345678Z", leido.getDni());
        assertEquals(LocalDate.of(1980, 5, 14), leido.getFechaNacimiento());
        assertTrue(otroArranque.buscar("PAC-9").isEmpty());
    }

    @Test
    @DisplayName("Guardar una entidad existente la actualiza sin duplicarla")
    void actualizar() {
        Paciente p = paciente("PAC-1", "12345678Z");
        pacientes.guardar(p);
        p.darDeBaja();
        pacientes.guardar(p);
        assertEquals(1, pacientes.todos().size());
        assertFalse(pacientes.obtener("PAC-1").isActivo());
    }

    @Test
    @DisplayName("todos() conserva el orden de inserción, como el repositorio en memoria")
    void orden() {
        pacientes.guardar(paciente("PAC-2", "87654321X"));
        pacientes.guardar(paciente("PAC-1", "12345678Z"));
        assertEquals(List.of("PAC-2", "PAC-1"), pacientes.todos().stream().map(Paciente::getId).toList());
        assertEquals(1, pacientes.filtrar(p -> p.getDni().startsWith("1")).size());
    }

    @Test
    @DisplayName("existe y eliminar; eliminar lo que no existe lanza NoEncontradoException")
    void eliminar() {
        pacientes.guardar(paciente("PAC-1", "12345678Z"));
        assertTrue(pacientes.existe("PAC-1"));
        pacientes.eliminar("PAC-1");
        assertFalse(pacientes.existe("PAC-1"));
        assertThrows(NoEncontradoException.class, () -> pacientes.eliminar("PAC-1"));
        assertThrows(NoEncontradoException.class, () -> pacientes.obtener("PAC-1"));
    }

    @Test
    @DisplayName("Una entidad nula no se guarda")
    void nula() {
        assertThrows(NullPointerException.class, () -> pacientes.guardar(null));
    }
}
