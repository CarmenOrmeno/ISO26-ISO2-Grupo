package es.uclm.esi.iso2.servidor.api;

import java.util.List;

import es.uclm.esi.iso2.servidor.catalogo.Modalidad;
import es.uclm.esi.iso2.servidor.catalogo.Prioridad;
import es.uclm.esi.iso2.servidor.citas.Cita;
import es.uclm.esi.iso2.servidor.esperas.EntradaEspera;

/** CU11 · Gestionar listas de espera, prioridades y huecos de agenda (personal de admisión). */
public interface ApiEsperas {

    List<EntradaEspera> listaDeEspera(String token);

    List<EntradaEspera> listaDeEspera(String token, Modalidad modalidad);

    void cambiarPrioridad(String token, long idSolicitud, Prioridad prioridad);

    Cita citarEnPrimerHueco(String token, long idSolicitud);

    /** Devuelve cuántas solicitudes se han citado. */
    int rellenarHuecos(String token, Modalidad modalidad);
}
