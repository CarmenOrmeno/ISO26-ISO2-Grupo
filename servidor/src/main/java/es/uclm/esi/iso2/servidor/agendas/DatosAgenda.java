package es.uclm.esi.iso2.servidor.agendas;

import java.time.LocalDate;
import java.time.LocalTime;

import es.uclm.esi.iso2.servidor.catalogo.Modalidad;

/** Datos para abrir la agenda de una sala y un médico en un día (CU05). */
public record DatosAgenda(LocalDate fecha, String sala, Modalidad modalidad, String loginMedico, LocalTime inicio,
        LocalTime fin, int minutosPorHueco) {
}
