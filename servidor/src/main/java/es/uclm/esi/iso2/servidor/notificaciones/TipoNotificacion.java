package es.uclm.esi.iso2.servidor.notificaciones;

/** Tipos de aviso al paciente (CU12). */
public enum TipoNotificacion {
    CITA_ASIGNADA,
    CITA_MODIFICADA,
    CITA_CANCELADA,
    RECORDATORIO,
    PREPARACION,
    INFORME_DISPONIBLE,
    SEGUIMIENTO
}
