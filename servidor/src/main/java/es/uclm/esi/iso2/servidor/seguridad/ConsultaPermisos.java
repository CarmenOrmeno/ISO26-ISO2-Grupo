package es.uclm.esi.iso2.servidor.seguridad;

/** Lo único que necesita {@link ControlAcceso} de la política de permisos (segregación de interfaces). */
public interface ConsultaPermisos {

    boolean tiene(Rol rol, Permiso permiso);
}
