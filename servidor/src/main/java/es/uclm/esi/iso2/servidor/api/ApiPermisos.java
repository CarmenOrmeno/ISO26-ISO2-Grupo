package es.uclm.esi.iso2.servidor.api;

import java.util.List;
import java.util.Set;

import es.uclm.esi.iso2.servidor.seguridad.Permiso;
import es.uclm.esi.iso2.servidor.seguridad.Rol;
import es.uclm.esi.iso2.servidor.seguridad.Usuario;

/** CU17 · Gestionar roles y permisos (administrador). Los cambios valen desde la siguiente operación. */
public interface ApiPermisos {

    Set<Permiso> permisosDe(String token, Rol rol);

    void conceder(String token, Rol rol, Permiso permiso);

    void revocar(String token, Rol rol, Permiso permiso);

    void restablecer(String token);

    List<Usuario> usuarios(String token);

    Usuario cambiarRol(String token, String login, Rol nuevoRol);
}
