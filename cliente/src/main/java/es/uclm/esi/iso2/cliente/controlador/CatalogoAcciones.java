package es.uclm.esi.iso2.cliente.controlador;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import es.uclm.esi.iso2.servidor.Servidor;
import es.uclm.esi.iso2.servidor.seguridad.Rol;

/**
 * Acciones del cliente por rol, construidas sobre las interfaces {@code api} del servidor (D-007). Todos los roles
 * empiezan por «Mi perfil» (CU01) y el médico tiene también las del paciente (D-005).
 */
public final class CatalogoAcciones {

    static final String GENERAL = "CU01 Mi cuenta";

    private CatalogoAcciones() {
    }

    public static Map<Rol, List<Accion>> desde(Servidor s) {
        Accion perfil = Accion.de(GENERAL, "Mi perfil", (t, v) -> s.sesion().usuarioActual(t));
        Map<Rol, List<Accion>> catalogo = new EnumMap<>(Rol.class);
        catalogo.put(Rol.PACIENTE, unir(List.of(perfil), AccionesPaciente.crear(s)));
        catalogo.put(Rol.MEDICO, unir(List.of(perfil), AccionesMedico.crear(s), AccionesPaciente.crear(s)));
        catalogo.put(Rol.ADMINISTRATIVO, unir(List.of(perfil), AccionesAdmision.crear(s)));
        catalogo.put(Rol.ADMINISTRADOR, unir(List.of(perfil), AccionesAdministrador.crear(s)));
        return catalogo;
    }

    /** Sesión del cliente con el catálogo de este servidor. */
    public static SesionCliente sesion(Servidor s) {
        return new SesionCliente(s.sesion(), desde(s));
    }

    @SafeVarargs
    private static List<Accion> unir(List<Accion>... listas) {
        List<Accion> todas = new ArrayList<>();
        for (List<Accion> l : listas) {
            todas.addAll(l);
        }
        return List.copyOf(todas);
    }
}
