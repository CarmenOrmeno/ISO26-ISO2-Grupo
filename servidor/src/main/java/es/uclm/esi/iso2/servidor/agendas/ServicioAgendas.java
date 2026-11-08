package es.uclm.esi.iso2.servidor.agendas;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import es.uclm.esi.iso2.servidor.catalogo.Modalidad;

/** Contrato interno del componente de agendas (CU05). */
public interface ServicioAgendas {

    Agenda crear(DatosAgenda datos);

    /** Solo si no tiene ningún hueco ocupado. */
    void eliminar(long idAgenda);

    Agenda obtener(long idAgenda);

    List<Agenda> delDia(LocalDate fecha);

    List<Agenda> todas();

    /** Huecos libres de una modalidad entre dos fechas (ambas incluidas), ordenados por fecha y hora. */
    List<HuecoLibre> huecosLibres(Modalidad modalidad, LocalDate desde, LocalDate hasta);

    void ocupar(long idAgenda, LocalTime hora, long idCita);

    void liberar(long idAgenda, LocalTime hora);
}
