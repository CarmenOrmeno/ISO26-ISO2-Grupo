package es.uclm.esi.iso2.servidor.auditoria;

import java.io.Serializable;
import java.time.LocalDateTime;

import es.uclm.esi.iso2.servidor.comun.Validaciones;
import es.uclm.esi.iso2.servidor.seguridad.Permiso;
import es.uclm.esi.iso2.servidor.seguridad.Rol;

/**
 * Constancia de un acceso (RNF-S2): quién (usuario y rol), qué operación (permiso), sobre qué recurso, cuándo
 * y si se permitió. Los registros no se modifican ni se borran.
 */
public record RegistroAuditoria(long id, LocalDateTime fecha, String login, Rol rol, Permiso operacion,
        String recurso, boolean permitido) implements Serializable {

    public RegistroAuditoria {
        Validaciones.obligatorio(fecha, "fecha");
        login = Validaciones.texto(login, "login");
        Validaciones.obligatorio(rol, "rol");
        Validaciones.obligatorio(operacion, "operación");
        recurso = recurso == null ? "" : recurso.strip();
    }

    @Override
    public String toString() {
        return fecha + " · " + login + " (" + rol + ") · " + operacion + (recurso.isEmpty() ? "" : " · " + recurso)
                + " · " + (permitido ? "PERMITIDO" : "DENEGADO");
    }
}
