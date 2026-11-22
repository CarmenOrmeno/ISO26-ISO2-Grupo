package es.uclm.esi.iso2.servidor.seguridad;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.uclm.esi.iso2.servidor.comun.NoEncontradoException;
import es.uclm.esi.iso2.servidor.comun.ReglaNegocioException;
import es.uclm.esi.iso2.servidor.comun.Repositorio;
import es.uclm.esi.iso2.servidor.persistencia.RepositorioEnMemoria;

/** CU17 · Reglas de la gestión de roles y permisos. */
class ServicioPermisosImplTest {

    private final Repositorio<PoliticaPermisos, String> almacen =
            new RepositorioEnMemoria<>(p -> ServicioPermisosImpl.ID_POLITICA);
    private final Repositorio<Usuario, String> usuarios = new RepositorioEnMemoria<>(Usuario::login);
    private final ServicioPermisos servicio = new ServicioPermisosImpl(almacen, usuarios);

    @Test
    @DisplayName("Sin política guardada se guarda y usa la de por defecto")
    void politicaInicial() {
        assertTrue(almacen.existe(ServicioPermisosImpl.ID_POLITICA));
        assertEquals(PoliticaPermisos.porDefecto().permisosDe(Rol.ADMINISTRADOR), servicio.permisosDe(Rol.ADMINISTRADOR));
    }

    @Test
    @DisplayName("Los cambios se guardan: otro servicio sobre el mismo almacén los ve (reinicio)")
    void seConserva() {
        servicio.revocar(Rol.PACIENTE, Permiso.RESPONDER_ENCUESTAS);
        servicio.conceder(Rol.ADMINISTRATIVO, Permiso.CONSULTAR_DISPONIBILIDAD);
        ServicioPermisos reiniciado = new ServicioPermisosImpl(almacen, usuarios);
        assertFalse(reiniciado.tiene(Rol.PACIENTE, Permiso.RESPONDER_ENCUESTAS));
        assertTrue(reiniciado.tiene(Rol.ADMINISTRATIVO, Permiso.CONSULTAR_DISPONIBILIDAD));
        reiniciado.restablecer();
        assertTrue(new ServicioPermisosImpl(almacen, usuarios).tiene(Rol.PACIENTE, Permiso.RESPONDER_ENCUESTAS));
    }

    @Test
    @DisplayName("El administrador no puede perder GESTIONAR_PERMISOS; otro rol sí puede perderlo")
    void administradorNoSeBloquea() {
        assertThrows(ReglaNegocioException.class, () -> servicio.revocar(Rol.ADMINISTRADOR, Permiso.GESTIONAR_PERMISOS));
        assertTrue(servicio.tiene(Rol.ADMINISTRADOR, Permiso.GESTIONAR_PERMISOS));
        servicio.conceder(Rol.ADMINISTRATIVO, Permiso.GESTIONAR_PERMISOS);
        servicio.revocar(Rol.ADMINISTRATIVO, Permiso.GESTIONAR_PERMISOS);
        assertFalse(servicio.tiene(Rol.ADMINISTRATIVO, Permiso.GESTIONAR_PERMISOS));
    }

    @Test
    @DisplayName("Cambiar el rol: se guarda; el propio rol no; usuario inexistente; paciente sin paciente asociado")
    void cambiarRol() {
        usuarios.guardar(new Usuario("admin1", "Admin", Rol.ADMINISTRADOR, null));
        usuarios.guardar(new Usuario("medico1", "Médico", Rol.MEDICO, null));
        assertEquals(Rol.ADMINISTRATIVO, servicio.cambiarRol("admin1", "medico1", Rol.ADMINISTRATIVO).rol());
        assertEquals(Rol.ADMINISTRATIVO, usuarios.obtener("medico1").rol());
        assertThrows(ReglaNegocioException.class, () -> servicio.cambiarRol("admin1", "admin1", Rol.MEDICO));
        assertThrows(NoEncontradoException.class, () -> servicio.cambiarRol("admin1", "nadie", Rol.MEDICO));
        assertThrows(IllegalArgumentException.class, () -> servicio.cambiarRol("admin1", "medico1", Rol.PACIENTE));
        assertThrows(IllegalArgumentException.class, () -> servicio.cambiarRol("admin1", "medico1", null));
        assertEquals(Rol.ADMINISTRATIVO, usuarios.obtener("medico1").rol());
    }

    @Test
    @DisplayName("Los usuarios se listan por login")
    void usuarios() {
        usuarios.guardar(new Usuario("zeta", "Z", Rol.ADMINISTRATIVO, null));
        usuarios.guardar(new Usuario("alfa", "A", Rol.MEDICO, null));
        assertEquals("alfa", servicio.usuarios().get(0).login());
    }
}
