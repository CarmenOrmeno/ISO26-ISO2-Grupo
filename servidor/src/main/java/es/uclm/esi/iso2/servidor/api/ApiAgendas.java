package es.uclm.esi.iso2.servidor.api;

import java.time.LocalDate;
import java.util.List;

import es.uclm.esi.iso2.servidor.agendas.Agenda;
import es.uclm.esi.iso2.servidor.agendas.DatosAgenda;
import es.uclm.esi.iso2.servidor.agendas.HuecoLibre;
import es.uclm.esi.iso2.servidor.catalogo.Modalidad;

/** CU05 · Gestionar agendas (personal de admisión). */
public interface ApiAgendas {

    Agenda crear(String token, DatosAgenda datos);

    void eliminar(String token, long idAgenda);

    Agenda obtener(String token, long idAgenda);

    List<Agenda> delDia(String token, LocalDate fecha);

    List<HuecoLibre> huecosLibres(String token, Modalidad modalidad, LocalDate desde, LocalDate hasta);
}
