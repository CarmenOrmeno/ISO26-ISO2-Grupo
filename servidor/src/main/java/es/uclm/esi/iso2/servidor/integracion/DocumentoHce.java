package es.uclm.esi.iso2.servidor.integracion;

import java.time.LocalDateTime;

/**
 * Documento clínico que se publica en la historia clínica electrónica del paciente (CU19). En un sistema
 * real sería un documento HL7 CDA o un recurso FHIR DiagnosticReport.
 *
 * @param referencia identificador del documento en nuestro sistema (p. ej. «informe 3»), para no duplicarlo
 */
public record DocumentoHce(String dniPaciente, String referencia, String titulo, String texto,
        LocalDateTime fecha) {
}
