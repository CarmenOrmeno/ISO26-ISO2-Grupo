package es.uclm.esi.iso2.servidor.api;

import es.uclm.esi.iso2.servidor.solicitudes.DatosSolicitud;
import es.uclm.esi.iso2.servidor.solicitudes.SolicitudPrueba;

/** CU04 · Solicitar prueba (médico). */
public interface ApiSolicitudes {

    /** El médico es el usuario de la sesión. */
    SolicitudPrueba solicitar(String token, DatosSolicitud datos);

    void cancelar(String token, long idSolicitud, String motivo);

    /** El médico solo ve sus solicitudes; el administrativo, todas. */
    SolicitudPrueba obtener(String token, long idSolicitud);
}
