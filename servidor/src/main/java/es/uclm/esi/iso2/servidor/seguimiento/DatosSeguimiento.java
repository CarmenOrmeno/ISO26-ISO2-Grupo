package es.uclm.esi.iso2.servidor.seguimiento;

import java.time.LocalDate;

import es.uclm.esi.iso2.servidor.catalogo.Prioridad;

/**
 * Datos que introduce el médico al prescribir un seguimiento (CU10).
 *
 * @param periodicidadMeses meses entre dos revisiones
 * @param numeroRevisiones  revisiones que se generarán en total
 * @param primeraRevision   fecha de la primera revisión (no anterior a hoy)
 */
public record DatosSeguimiento(String idPaciente, String codigoTipo, String indicacionClinica, String justificacion,
        Prioridad prioridad, int periodicidadMeses, int numeroRevisiones, LocalDate primeraRevision) {
}
