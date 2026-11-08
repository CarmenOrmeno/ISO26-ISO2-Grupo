package es.uclm.esi.iso2.servidor.seguridad;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PoliticaPermisosTest {

    private final PoliticaPermisos politica = PoliticaPermisos.porDefecto();

    @Test
    @DisplayName("D-005: el médico tiene todos los permisos del paciente")
    void medicoIncluyePaciente() {
        assertTrue(politica.permisosDe(Rol.MEDICO).containsAll(politica.permisosDe(Rol.PACIENTE)));
    }

    @Test
    @DisplayName("Todos los roles pueden consultar su perfil")
    void todosConsultanPerfil() {
        for (Rol rol : Rol.values()) {
            assertTrue(politica.tiene(rol, Permiso.CONSULTAR_PERFIL), rol.name());
        }
    }

    @Test
    @DisplayName("El conjunto devuelto no se puede modificar desde fuera")
    void inmodificable() {
        assertThrows(UnsupportedOperationException.class,
                () -> politica.permisosDe(Rol.PACIENTE).add(Permiso.CONSULTAR_PERFIL));
    }
}
