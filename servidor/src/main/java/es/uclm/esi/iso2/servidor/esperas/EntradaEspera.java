package es.uclm.esi.iso2.servidor.esperas;

import java.time.LocalDate;

import es.uclm.esi.iso2.servidor.catalogo.Modalidad;
import es.uclm.esi.iso2.servidor.catalogo.Prioridad;

/**
 * Solicitud que espera cita, vista desde la lista de espera (CU11).
 *
 * @param fechaLimite  fecha de solicitud + plazo máximo de su prioridad (CU03)
 * @param diasEspera   días transcurridos desde la solicitud
 * @param fueraDePlazo hoy ya es posterior a la fecha límite
 */
public record EntradaEspera(long idSolicitud, String idPaciente, String codigoTipo, Modalidad modalidad,
        Prioridad prioridad, LocalDate fechaSolicitud, LocalDate fechaLimite, long diasEspera,
        boolean fueraDePlazo) {

    @Override
    public String toString() {
        return "Solicitud " + idSolicitud + " · " + prioridad + " · " + codigoTipo + " · paciente " + idPaciente
                + " · " + diasEspera + " días · límite " + fechaLimite + (fueraDePlazo ? " · FUERA DE PLAZO" : "");
    }
}
