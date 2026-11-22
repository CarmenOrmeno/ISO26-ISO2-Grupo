package es.uclm.esi.iso2.servidor.api;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import es.uclm.esi.iso2.servidor.citas.Cita;
import es.uclm.esi.iso2.servidor.solicitudes.SolicitudPrueba;

/** CU06 · Gestionar citas (personal de admisión). */
public interface ApiCitas {

    /** Solicitudes que esperan cita. */
    List<SolicitudPrueba> solicitudesPendientes(String token);

    Cita asignar(String token, long idSolicitud, long idAgenda, LocalTime hora);

    Cita reprogramar(String token, long idCita, long idAgenda, LocalTime hora);

    void cancelar(String token, long idCita, String motivo);

    Cita obtener(String token, long idCita);

    List<Cita> delDia(String token, LocalDate fecha);

    List<Cita> dePaciente(String token, String idPaciente);

    /** CU07: citas en las que el paciente ha pedido un cambio. */
    List<Cita> conCambioSolicitado(String token);
}
