package es.uclm.esi.iso2.servidor.seguridad;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

class UsuarioTest {

    @ParameterizedTest(name = "rol {0} sin paciente asociado es válido")
    @EnumSource(value = Rol.class, names = "PACIENTE", mode = EnumSource.Mode.EXCLUDE)
    void rolesNoPacienteSinIdPaciente(Rol rol) {
        assertNull(new Usuario("u", "Nombre", rol, null).idPaciente());
    }

    @ParameterizedTest(name = "paciente con idPaciente «{0}» es inválido")
    @NullAndEmptySource
    @DisplayName("Un usuario paciente exige paciente asociado")
    void pacienteSinIdPaciente(String idPaciente) {
        assertThrows(IllegalArgumentException.class, () -> new Usuario("u", "N", Rol.PACIENTE, idPaciente));
    }

    @Test
    @DisplayName("Login, nombre y rol son obligatorios")
    void obligatorios() {
        assertThrows(IllegalArgumentException.class, () -> new Usuario(" ", "N", Rol.MEDICO, null));
        assertThrows(IllegalArgumentException.class, () -> new Usuario("u", null, Rol.MEDICO, null));
        assertThrows(IllegalArgumentException.class, () -> new Usuario("u", "N", null, null));
    }

    @Test
    @DisplayName("conRol devuelve una copia con el nuevo rol")
    void conRol() {
        Usuario u = new Usuario("u", "N", Rol.PACIENTE, "PAC-1");
        Usuario medico = u.conRol(Rol.MEDICO);
        assertEquals(Rol.MEDICO, medico.rol());
        assertEquals("PAC-1", medico.idPaciente());
        assertEquals(Rol.PACIENTE, u.rol());
    }
}
