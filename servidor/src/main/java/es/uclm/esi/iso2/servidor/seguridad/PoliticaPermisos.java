package es.uclm.esi.iso2.servidor.seguridad;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/** Asignación de permisos a roles (D-011). Configurable en ejecución por el administrador (CU17). */
public class PoliticaPermisos {

    private final Map<Rol, EnumSet<Permiso>> permisos = new EnumMap<>(Rol.class);

    public PoliticaPermisos() {
        for (Rol rol : Rol.values()) {
            permisos.put(rol, EnumSet.noneOf(Permiso.class));
        }
    }

    /** Política inicial del SESCAM. El médico incluye los permisos del paciente (D-005). */
    public static PoliticaPermisos porDefecto() {
        PoliticaPermisos p = new PoliticaPermisos();
        EnumSet<Permiso> paciente = EnumSet.of(Permiso.CONSULTAR_PERFIL);
        p.permisos.get(Rol.PACIENTE).addAll(paciente);

        p.permisos.get(Rol.MEDICO).addAll(paciente);

        p.permisos.get(Rol.ADMINISTRATIVO).addAll(EnumSet.of(Permiso.CONSULTAR_PERFIL));

        p.permisos.get(Rol.ADMINISTRADOR).addAll(EnumSet.of(Permiso.CONSULTAR_PERFIL));
        return p;
    }

    public boolean tiene(Rol rol, Permiso permiso) {
        return permisos.get(rol).contains(permiso);
    }

    public Set<Permiso> permisosDe(Rol rol) {
        return Collections.unmodifiableSet(EnumSet.copyOf(permisos.get(rol)));
    }

    public void conceder(Rol rol, Permiso permiso) {
        permisos.get(rol).add(permiso);
    }

    public void revocar(Rol rol, Permiso permiso) {
        permisos.get(rol).remove(permiso);
    }
}
