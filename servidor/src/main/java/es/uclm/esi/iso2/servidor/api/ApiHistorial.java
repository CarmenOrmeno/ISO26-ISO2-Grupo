package es.uclm.esi.iso2.servidor.api;

import java.time.LocalDate;
import java.util.List;

import es.uclm.esi.iso2.servidor.historial.EntradaHistorial;
import es.uclm.esi.iso2.servidor.informes.Informe;

/** CU13 · Consultar el historial de pruebas realizadas (paciente; el médico lo hereda). */
public interface ApiHistorial {

    List<EntradaHistorial> miHistorial(String token);

    List<EntradaHistorial> miHistorial(String token, LocalDate desde, LocalDate hasta);

    /** Informe de una de sus pruebas. */
    Informe miInforme(String token, long idSolicitud);
}
