package es.uclm.esi.iso2.servidor.solicitudes;

import es.uclm.esi.iso2.servidor.catalogo.Prioridad;

/** Datos que introduce el médico al solicitar una prueba (CU04). */
public record DatosSolicitud(String idPaciente, String codigoTipo, String indicacionClinica, String justificacion,
        Prioridad prioridad) {
}
