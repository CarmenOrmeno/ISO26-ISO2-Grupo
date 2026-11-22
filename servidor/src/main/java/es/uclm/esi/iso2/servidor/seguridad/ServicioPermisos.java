package es.uclm.esi.iso2.servidor.seguridad;

import java.util.List;
import java.util.Set;

/** Contrato interno de la gestión de roles y permisos (CU17). */
public interface ServicioPermisos extends ConsultaPermisos {

    Set<Permiso> permisosDe(Rol rol);

    void conceder(Rol rol, Permiso permiso);

    /** El administrador no puede perder {@link Permiso#GESTIONAR_PERMISOS} (se quedaría sin poder deshacerlo). */
    void revocar(Rol rol, Permiso permiso);

    /** Vuelve a la política por defecto. */
    void restablecer();

    List<Usuario> usuarios();

    /** Cambia el rol de un usuario. Nadie puede cambiarse su propio rol (siempre queda un administrador). */
    Usuario cambiarRol(String loginQuienCambia, String login, Rol nuevoRol);
}
