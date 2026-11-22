package es.uclm.esi.iso2.servidor.api;

import java.time.LocalDate;
import java.util.List;

import es.uclm.esi.iso2.servidor.agendas.Agenda;
import es.uclm.esi.iso2.servidor.agendas.HuecoLibre;
import es.uclm.esi.iso2.servidor.catalogo.Modalidad;
import es.uclm.esi.iso2.servidor.citas.SituacionSolicitud;
import es.uclm.esi.iso2.servidor.solicitudes.EstadoSolicitud;
import es.uclm.esi.iso2.servidor.solicitudes.SolicitudPrueba;

/** CU14 · Consultar el estado de sus solicitudes y las agendas disponibles (médico). */
public interface ApiEstadoSolicitudes {

    List<SolicitudPrueba> misSolicitudes(String token);

    List<SolicitudPrueba> misSolicitudes(String token, EstadoSolicitud estado);

    /** Solo de sus propias solicitudes. */
    SituacionSolicitud situacion(String token, long idSolicitud);

    /** Agendas en las que el médico de la sesión atiende, entre dos fechas (ambas incluidas). */
    List<Agenda> misAgendas(String token, LocalDate desde, LocalDate hasta);

    List<HuecoLibre> huecosLibres(String token, Modalidad modalidad, LocalDate desde, LocalDate hasta);
}
