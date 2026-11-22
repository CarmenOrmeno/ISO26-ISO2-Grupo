package es.uclm.esi.iso2.servidor.seguimiento;

/** Ciclo de vida de un seguimiento periódico (CU10). */
public enum EstadoSeguimiento {
    /** Quedan revisiones por generar. */
    ACTIVO,
    /** Se han generado todas las revisiones prescritas. */
    FINALIZADO,
    /** El médico lo ha cancelado antes de terminar. */
    CANCELADO
}
