package es.uclm.esi.iso2.servidor.pacientes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import es.uclm.esi.iso2.servidor.comun.ReglaNegocioException;

class PacienteTest {

    static final LocalDate HOY = LocalDate.of(2026, 11, 2);

    static DatosPaciente datos() {
        return new DatosPaciente("12345678Z", "Lucía", "Martín", LocalDate.of(1980, 5, 14), "600111222", "l@x.es");
    }

    static DatosPaciente conNacimiento(LocalDate fecha) {
        return new DatosPaciente("12345678Z", "Lucía", "Martín", fecha, "600111222", null);
    }

    static DatosPaciente conTelefono(String tel) {
        return new DatosPaciente("12345678Z", "Lucía", "Martín", LocalDate.of(1980, 5, 14), tel, null);
    }

    @Test
    @DisplayName("Alta con datos válidos")
    void altaValida() {
        Paciente p = new Paciente("PAC-1", datos(), HOY);
        assertEquals("Lucía Martín", p.getNombreCompleto());
        assertTrue(p.isActivo());
        assertTrue(p.toString().contains("PAC-1"));
    }

    @ParameterizedTest(name = "nacimiento {0} → válido={1}")
    @CsvSource({ "1899-12-31,false", "1900-01-01,true", "2026-11-02,true", "2026-11-03,false" })
    @DisplayName("Valores límite de la fecha de nacimiento [01/01/1900, hoy]")
    void limitesNacimiento(LocalDate fecha, boolean valida) {
        if (valida) {
            assertEquals(fecha, new Paciente("PAC-1", conNacimiento(fecha), HOY).getFechaNacimiento());
        } else {
            assertThrows(IllegalArgumentException.class, () -> new Paciente("PAC-1", conNacimiento(fecha), HOY));
        }
    }

    @ParameterizedTest(name = "teléfono «{0}» inválido")
    @ValueSource(strings = { "", "500111222", "60011122", "6001112223", "60011122a" })
    @DisplayName("Teléfonos inválidos: vacío, prefijo, longitud 8 y 10, letras")
    void telefonosInvalidos(String tel) {
        assertThrows(IllegalArgumentException.class, () -> new Paciente("PAC-1", conTelefono(tel), HOY));
    }

    @ParameterizedTest(name = "teléfono «{0}» válido")
    @ValueSource(strings = { "600111222", "700111222", "800111222", "925000111" })
    void telefonosValidos(String tel) {
        assertEquals(tel, new Paciente("PAC-1", conTelefono(tel), HOY).getTelefono());
    }

    @Test
    @DisplayName("Correo opcional: vacío se guarda como null; mal formado se rechaza")
    void correo() {
        DatosPaciente vacio = new DatosPaciente("12345678Z", "L", "M", LocalDate.of(1980, 1, 1), "600111222", "  ");
        assertNull(new Paciente("PAC-1", vacio, HOY).getEmail());
        DatosPaciente malo = new DatosPaciente("12345678Z", "L", "M", LocalDate.of(1980, 1, 1), "600111222", "sin-arroba");
        assertThrows(IllegalArgumentException.class, () -> new Paciente("PAC-1", malo, HOY));
    }

    @Test
    @DisplayName("Campos obligatorios vacíos o nulos")
    void obligatorios() {
        assertThrows(IllegalArgumentException.class, () -> new Paciente("PAC-1", null, HOY));
        assertThrows(IllegalArgumentException.class, () -> new Paciente("PAC-1",
                new DatosPaciente("12345678Z", "", "M", LocalDate.of(1980, 1, 1), "600111222", null), HOY));
        assertThrows(IllegalArgumentException.class, () -> new Paciente("PAC-1",
                new DatosPaciente("12345678Z", "L", null, LocalDate.of(1980, 1, 1), "600111222", null), HOY));
        assertThrows(IllegalArgumentException.class, () -> new Paciente("PAC-1", conNacimiento(null), HOY));
    }

    @Test
    @DisplayName("Modificar cambia los datos pero no el DNI")
    void modificar() {
        Paciente p = new Paciente("PAC-1", datos(), HOY);
        p.modificar(new DatosPaciente("12345678z", "Lucía", "Martín Pérez", LocalDate.of(1980, 5, 14), "699000000", null), HOY);
        assertEquals("Martín Pérez", p.getApellidos());
        assertEquals("699000000", p.getTelefono());
        assertThrows(ReglaNegocioException.class, () -> p.modificar(
                new DatosPaciente("87654321X", "L", "M", LocalDate.of(1980, 1, 1), "600111222", null), HOY));
        assertThrows(IllegalArgumentException.class, () -> p.modificar(null, HOY));
    }

    @Test
    @DisplayName("Baja lógica: no se puede dar de baja dos veces ni modificar tras la baja")
    void baja() {
        Paciente p = new Paciente("PAC-1", datos(), HOY);
        p.darDeBaja();
        assertFalse(p.isActivo());
        assertTrue(p.toString().contains("BAJA"));
        assertThrows(ReglaNegocioException.class, p::darDeBaja);
        assertThrows(ReglaNegocioException.class, () -> p.modificar(datos(), HOY));
    }
}
