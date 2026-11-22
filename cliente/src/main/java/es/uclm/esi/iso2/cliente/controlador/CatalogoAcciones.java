package es.uclm.esi.iso2.cliente.controlador;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import es.uclm.esi.iso2.servidor.api.Apis;
import es.uclm.esi.iso2.servidor.seguridad.Rol;

/**
 * Acciones del cliente por rol, construidas sobre las interfaces {@code api} del servidor (D-007): cada catálogo
 * recibe solo las interfaces que usa (ISP). Todos los roles empiezan por «Mi perfil» (CU01) y el médico tiene
 * también las del paciente (D-005).
 */
public final class CatalogoAcciones {

    static final String GENERAL = "CU01 Mi cuenta";

    private CatalogoAcciones() {
    }

    public static Map<Rol, List<Accion>> desde(Apis a) {
        Accion perfil = Accion.de(GENERAL, "Mi perfil", (t, v) -> a.sesion().usuarioActual(t));
        List<Accion> paciente = AccionesPaciente.crear(a.misCitas(), a.notificaciones(), a.historial(),
                a.atencionPaciente(), a.misSeguimientos());
        Map<Rol, List<Accion>> catalogo = new EnumMap<>(Rol.class);
        catalogo.put(Rol.PACIENTE, unir(List.of(perfil), paciente));
        catalogo.put(Rol.MEDICO, unir(List.of(perfil), AccionesMedico.crear(a.catalogo(), a.solicitudes(),
                a.estadoSolicitudes(), a.realizacion(), a.informes(), a.seguimiento(), a.estadisticasMedico()),
                paciente));
        catalogo.put(Rol.ADMINISTRATIVO, unir(List.of(perfil), AccionesAdmision.crear(a.pacientes(), a.agendas(),
                a.citas(), a.esperas(), a.atencion(), a.informesOperativos())));
        catalogo.put(Rol.ADMINISTRADOR, unir(List.of(perfil), AccionesAdministrador.crear(a.catalogo(),
                a.notificaciones(), a.seguimiento(), a.permisos(), a.auditoria(), a.integracion(),
                a.estadisticasGlobales())));
        return catalogo;
    }

    /** Sesión del cliente con el catálogo de este servidor. */
    public static SesionCliente sesion(Apis a) {
        return new SesionCliente(a.sesion(), desde(a));
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
