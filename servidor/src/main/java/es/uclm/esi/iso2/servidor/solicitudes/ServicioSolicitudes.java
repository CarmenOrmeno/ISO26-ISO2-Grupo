package es.uclm.esi.iso2.servidor.solicitudes;

import java.util.List;

/** Contrato interno del componente de solicitudes. */
public interface ServicioSolicitudes {

    /** CU04: el médico solicita una prueba para un paciente activo y un tipo del catálogo. */
    SolicitudPrueba solicitar(String loginMedico, DatosSolicitud datos);

    /** Solo el médico que la pidió puede cancelarla, y solo si aún no está citada. */
    void cancelar(long idSolicitud, String loginMedico, String motivo);

    SolicitudPrueba obtener(long idSolicitud);

    List<SolicitudPrueba> porMedico(String loginMedico);

    List<SolicitudPrueba> porPaciente(String idPaciente);

    /** Solicitudes que esperan cita (estado SOLICITADA). */
    List<SolicitudPrueba> pendientes();

    List<SolicitudPrueba> todas();
}
