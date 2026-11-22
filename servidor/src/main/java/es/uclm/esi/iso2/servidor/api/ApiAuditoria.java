package es.uclm.esi.iso2.servidor.api;

import java.time.LocalDate;
import java.util.List;

import es.uclm.esi.iso2.servidor.auditoria.RegistroAuditoria;

/**
 * CU18 · Auditar actividades y accesos a datos clínicos (administrador). Del más reciente al más antiguo,
 * entre dos fechas (ambas incluidas).
 */
public interface ApiAuditoria {

    List<RegistroAuditoria> registros(String token, LocalDate desde, LocalDate hasta);

    List<RegistroAuditoria> registrosDeUsuario(String token, String login, LocalDate desde, LocalDate hasta);

    List<RegistroAuditoria> accesosDenegados(String token, LocalDate desde, LocalDate hasta);

    /** Accesos a un recurso: el texto se busca en el recurso (p. ej. «solicitud 7»). */
    List<RegistroAuditoria> accesosARecurso(String token, String texto, LocalDate desde, LocalDate hasta);
}
