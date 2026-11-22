package es.uclm.esi.iso2.servidor.controlador;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import es.uclm.esi.iso2.servidor.api.ApiPermisos;
import es.uclm.esi.iso2.servidor.catalogo.Modalidad;
import es.uclm.esi.iso2.servidor.comun.AccesoDenegadoException;
import es.uclm.esi.iso2.servidor.comun.ReglaNegocioException;
import es.uclm.esi.iso2.servidor.seguridad.Permiso;
import es.uclm.esi.iso2.servidor.seguridad.Rol;
import es.uclm.esi.iso2.servidor.seguridad.Usuario;

/** It12 · CU17 Gestionar roles y permisos. */
class ControladorPermisosTest {

    private final Entorno e = new Entorno().conDatosBasicos();
    private final ApiPermisos permisos = e.servidor.permisos();
    private final String admin = e.token("admin1");

    @Test
    @DisplayName("CU17: el administrador consulta los permisos de cada rol (política por defecto)")
    void consultar() {
        assertTrue(permisos.permisosDe(admin, Rol.ADMINISTRADOR).contains(Permiso.GESTIONAR_PERMISOS));
        assertTrue(permisos.permisosDe(admin, Rol.MEDICO).contains(Permiso.CONSULTAR_MI_HISTORIAL));
        assertFalse(permisos.permisosDe(admin, Rol.PACIENTE).contains(Permiso.SOLICITAR_PRUEBAS));
    }

    @Test
    @DisplayName("CU17: revocar un permiso deniega la operación desde la siguiente llamada, sin cerrar sesión")
    void revocarSurteEfecto() {
        String paciente = e.token("paciente1");
        assertDoesNotThrow(() -> e.servidor.historial().miHistorial(paciente));
        permisos.revocar(admin, Rol.PACIENTE, Permiso.CONSULTAR_MI_HISTORIAL);
        assertThrows(AccesoDenegadoException.class, () -> e.servidor.historial().miHistorial(paciente));
        permisos.restablecer(admin);
        assertDoesNotThrow(() -> e.servidor.historial().miHistorial(paciente));
    }

    @Test
    @DisplayName("CU17: conceder un permiso a otro rol le da acceso a la operación")
    void concederSurteEfecto() {
        String admision = e.token("admision1");
        assertThrows(AccesoDenegadoException.class,
                () -> e.servidor.estadoSolicitudes().huecosLibres(admision, Modalidad.RX, Entorno.MANANA, Entorno.MANANA));
        permisos.conceder(admin, Rol.ADMINISTRATIVO, Permiso.CONSULTAR_DISPONIBILIDAD);
        assertDoesNotThrow(() -> e.servidor.estadoSolicitudes().huecosLibres(admision, Modalidad.RX, Entorno.MANANA, Entorno.MANANA));
    }

    @Test
    @DisplayName("CU17: el administrador no puede quitarse el permiso de gestionar permisos")
    void noSeBloquea() {
        assertThrows(ReglaNegocioException.class,
                () -> permisos.revocar(admin, Rol.ADMINISTRADOR, Permiso.GESTIONAR_PERMISOS));
        assertDoesNotThrow(() -> permisos.usuarios(admin));
    }

    @Test
    @DisplayName("CU17: cambiar el rol de un usuario vale en su sesión abierta; el propio rol no se cambia")
    void cambiarRol() {
        String medico2 = e.token("medico2");
        Usuario cambiado = permisos.cambiarRol(admin, "medico2", Rol.ADMINISTRATIVO);
        assertEquals(Rol.ADMINISTRATIVO, cambiado.rol());
        assertThrows(AccesoDenegadoException.class, () -> e.servidor.estadoSolicitudes().misSolicitudes(medico2));
        assertThrows(ReglaNegocioException.class, () -> permisos.cambiarRol(admin, "admin1", Rol.MEDICO));
        assertThrows(IllegalArgumentException.class, () -> permisos.cambiarRol(admin, "medico1", Rol.PACIENTE));
        assertEquals(6, permisos.usuarios(admin).size());
    }

    @ParameterizedTest(name = "{0} no gestiona permisos")
    @ValueSource(strings = { "paciente1", "medico1", "admision1" })
    void sinPermiso(String login) {
        String t = e.token(login);
        assertThrows(AccesoDenegadoException.class, () -> permisos.permisosDe(t, Rol.PACIENTE));
        assertThrows(AccesoDenegadoException.class, () -> permisos.conceder(t, Rol.PACIENTE, Permiso.CONFIGURAR_SISTEMA));
        assertThrows(AccesoDenegadoException.class, () -> permisos.cambiarRol(t, "medico2", Rol.ADMINISTRADOR));
    }
}
