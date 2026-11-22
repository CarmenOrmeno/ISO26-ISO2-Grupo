package es.uclm.esi.iso2.servidor.api;

import java.util.List;

import es.uclm.esi.iso2.servidor.seguimiento.Seguimiento;
import es.uclm.esi.iso2.servidor.solicitudes.SolicitudPrueba;

/** CU16 · Consultar seguimientos y revisiones periódicas (paciente; el médico lo hereda). */
public interface ApiMisSeguimientos {

    List<Seguimiento> misSeguimientos(String token);

    /** Solicitudes generadas por uno de sus seguimientos, con su estado. */
    List<SolicitudPrueba> revisiones(String token, long idSeguimiento);
}
