package es.uclm.esi.iso2.servidor.auditoria;

import java.time.LocalDate;
import java.util.List;

import es.uclm.esi.iso2.servidor.seguridad.Auditor;

/**
 * Contrato interno de la auditoría (CU18). Registra los accesos que le pasa el control de acceso y permite
 * consultarlos. Todas las consultas devuelven los registros del más reciente al más antiguo, entre dos
 * fechas (ambas incluidas).
 */
public interface ServicioAuditoria extends Auditor {

    List<RegistroAuditoria> registros(LocalDate desde, LocalDate hasta);

    List<RegistroAuditoria> deUsuario(String login, LocalDate desde, LocalDate hasta);

    List<RegistroAuditoria> denegados(LocalDate desde, LocalDate hasta);

    /** Registros cuyo recurso contiene el texto, sin distinguir mayúsculas (p. ej. «solicitud 7», «PAC-1»). */
    List<RegistroAuditoria> sobreRecurso(String texto, LocalDate desde, LocalDate hasta);
}
