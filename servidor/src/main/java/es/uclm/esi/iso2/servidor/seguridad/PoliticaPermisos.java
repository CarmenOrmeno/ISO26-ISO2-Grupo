package es.uclm.esi.iso2.servidor.seguridad;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import es.uclm.esi.iso2.servidor.comun.Validaciones;

/**
 * Asignación de permisos a roles (D-011), configurable por el administrador (CU17).
 * <p>
 * Solo se guardan los cambios respecto a la base (la política por defecto o ninguna): los permisos
 * concedidos de más y los revocados. Así, un permiso que se añada en una versión posterior llega con sus
 * roles por defecto aunque la política ya esté guardada en la base de datos (D-018).
 */
public class PoliticaPermisos implements ConsultaPermisos, Serializable {

    private static final long serialVersionUID = 1L;

    private final boolean basePorDefecto;
    private final Map<Rol, EnumSet<Permiso>> concedidos = new EnumMap<>(Rol.class);
    private final Map<Rol, EnumSet<Permiso>> revocados = new EnumMap<>(Rol.class);

    /** Política vacía: ningún rol tiene ningún permiso hasta que se le concede. */
    public PoliticaPermisos() {
        this(false);
    }

    private PoliticaPermisos(boolean basePorDefecto) {
        this.basePorDefecto = basePorDefecto;
        for (Rol rol : Rol.values()) {
            concedidos.put(rol, EnumSet.noneOf(Permiso.class));
            revocados.put(rol, EnumSet.noneOf(Permiso.class));
        }
    }

    /** Política inicial del SESCAM. El médico incluye los permisos del paciente (D-005). */
    public static PoliticaPermisos porDefecto() {
        return new PoliticaPermisos(true);
    }

    /** El rol tiene el permiso en la política por defecto. */
    public static boolean porDefecto(Rol rol, Permiso permiso) {
        Set<Rol> roles = permiso.rolesPorDefecto();
        return roles.contains(rol) || (rol == Rol.MEDICO && roles.contains(Rol.PACIENTE));
    }

    private boolean enLaBase(Rol rol, Permiso permiso) {
        return basePorDefecto && porDefecto(rol, permiso);
    }

    @Override
    public boolean tiene(Rol rol, Permiso permiso) {
        if (concedidos.get(rol).contains(permiso)) {
            return true;
        }
        return !revocados.get(rol).contains(permiso) && enLaBase(rol, permiso);
    }

    public Set<Permiso> permisosDe(Rol rol) {
        Validaciones.obligatorio(rol, "rol");
        return Collections.unmodifiableSet(Arrays.stream(Permiso.values())
                .filter(p -> tiene(rol, p))
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(Permiso.class))));
    }

    public void conceder(Rol rol, Permiso permiso) {
        comprobar(rol, permiso);
        revocados.get(rol).remove(permiso);
        if (!enLaBase(rol, permiso)) {
            concedidos.get(rol).add(permiso);
        }
    }

    public void revocar(Rol rol, Permiso permiso) {
        comprobar(rol, permiso);
        concedidos.get(rol).remove(permiso);
        if (enLaBase(rol, permiso)) {
            revocados.get(rol).add(permiso);
        }
    }

    /** Deshace todos los cambios: vuelve a la base. */
    public void restablecer() {
        concedidos.values().forEach(Set::clear);
        revocados.values().forEach(Set::clear);
    }

    /** Número de cambios respecto a la base (0 = política sin modificar). */
    public int cambios() {
        return concedidos.values().stream().mapToInt(Set::size).sum()
                + revocados.values().stream().mapToInt(Set::size).sum();
    }

    private static void comprobar(Rol rol, Permiso permiso) {
        Validaciones.obligatorio(rol, "rol");
        Validaciones.obligatorio(permiso, "permiso");
    }
}
