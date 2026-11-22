package es.uclm.esi.iso2.servidor.historial;

import java.time.LocalDate;
import java.util.List;

import es.uclm.esi.iso2.servidor.informes.Informe;

/** Contrato interno del historial de pruebas del paciente (CU13). */
public interface ServicioHistorial {

    /** Pruebas realizadas (REALIZADA o INFORMADA) del paciente, de la más reciente a la más antigua. */
    List<EntradaHistorial> historial(String idPaciente);

    /** Igual, solo las realizadas entre dos fechas (ambas incluidas). */
    List<EntradaHistorial> historial(String idPaciente, LocalDate desde, LocalDate hasta);

    /** Informe de una prueba del propio paciente; si es de otro paciente, AccesoDenegadoException. */
    Informe informe(String idPaciente, long idSolicitud);
}
