package es.uclm.esi.iso2.servidor.seguridad;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

    @Test
    @DisplayName("CU17: conceder y revocar sobre la política por defecto; se cuentan solo los cambios")
    void concederRevocar() {
        assertEquals(0, politica.cambios());
        politica.conceder(Rol.ADMINISTRATIVO, Permiso.CONSULTAR_DISPONIBILIDAD);
        politica.revocar(Rol.PACIENTE, Permiso.CONSULTAR_MI_HISTORIAL);
        assertTrue(politica.tiene(Rol.ADMINISTRATIVO, Permiso.CONSULTAR_DISPONIBILIDAD));
        assertFalse(politica.tiene(Rol.PACIENTE, Permiso.CONSULTAR_MI_HISTORIAL));
        // el médico lo tenía heredado del paciente: revocarlo al paciente no se lo quita al médico
        assertTrue(politica.tiene(Rol.MEDICO, Permiso.CONSULTAR_MI_HISTORIAL));
        assertEquals(2, politica.cambios());
    }

    @Test
    @DisplayName("CU17: conceder lo que ya se tiene o revocar lo que no se tiene no cuenta como cambio")
    void operacionesRepetidas() {
        politica.conceder(Rol.PACIENTE, Permiso.CONSULTAR_PERFIL);
        politica.revocar(Rol.PACIENTE, Permiso.CONFIGURAR_SISTEMA);
        assertEquals(0, politica.cambios());
        politica.revocar(Rol.PACIENTE, Permiso.CONSULTAR_PERFIL);
        politica.conceder(Rol.PACIENTE, Permiso.CONSULTAR_PERFIL);
        assertEquals(0, politica.cambios());
        assertTrue(politica.tiene(Rol.PACIENTE, Permiso.CONSULTAR_PERFIL));
    }

    @Test
    @DisplayName("CU17: restablecer vuelve a la política por defecto")
    void restablecer() {
        politica.conceder(Rol.PACIENTE, Permiso.CONFIGURAR_SISTEMA);
        politica.revocar(Rol.MEDICO, Permiso.SOLICITAR_PRUEBAS);
        politica.restablecer();
        assertEquals(0, politica.cambios());
        assertEquals(PoliticaPermisos.porDefecto().permisosDe(Rol.MEDICO), politica.permisosDe(Rol.MEDICO));
        assertFalse(politica.tiene(Rol.PACIENTE, Permiso.CONFIGURAR_SISTEMA));
    }

    @Test
    @DisplayName("Una política vacía no da ningún permiso y al restablecerla sigue vacía")
    void vacia() {
        PoliticaPermisos vacia = new PoliticaPermisos();
        vacia.conceder(Rol.PACIENTE, Permiso.CONSULTAR_PERFIL);
        vacia.restablecer();
        for (Rol rol : Rol.values()) {
            assertTrue(vacia.permisosDe(rol).isEmpty(), rol.name());
        }
    }

    @Test
    @DisplayName("Rol o permiso nulos → IllegalArgumentException")
    void nulos() {
        assertThrows(IllegalArgumentException.class, () -> politica.conceder(null, Permiso.CONSULTAR_PERFIL));
        assertThrows(IllegalArgumentException.class, () -> politica.revocar(Rol.PACIENTE, null));
        assertThrows(IllegalArgumentException.class, () -> politica.permisosDe(null));
    }
}
