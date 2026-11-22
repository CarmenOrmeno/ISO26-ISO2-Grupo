package es.uclm.esi.iso2.servidor.controlador;

import java.time.LocalDate;
import java.util.List;

import es.uclm.esi.iso2.servidor.agendas.Agenda;
import es.uclm.esi.iso2.servidor.agendas.HuecoLibre;
import es.uclm.esi.iso2.servidor.agendas.ServicioAgendas;
import es.uclm.esi.iso2.servidor.api.ApiEstadoSolicitudes;
import es.uclm.esi.iso2.servidor.catalogo.Modalidad;
import es.uclm.esi.iso2.servidor.citas.ServicioCitas;
import es.uclm.esi.iso2.servidor.citas.SituacionSolicitud;
import es.uclm.esi.iso2.servidor.comun.AccesoDenegadoException;
import es.uclm.esi.iso2.servidor.seguridad.ControlAcceso;
import es.uclm.esi.iso2.servidor.seguridad.Permiso;
import es.uclm.esi.iso2.servidor.solicitudes.EstadoSolicitud;
import es.uclm.esi.iso2.servidor.solicitudes.ServicioSolicitudes;
import es.uclm.esi.iso2.servidor.solicitudes.SolicitudPrueba;

/** El médico solo sigue sus propias solicitudes (RNF-S3). */
public class ControladorEstadoSolicitudes implements ApiEstadoSolicitudes {

    private final ServicioSolicitudes solicitudes;
    private final ServicioCitas citas;
    private final ServicioAgendas agendas;
    private final ControlAcceso acceso;

    public ControladorEstadoSolicitudes(ServicioSolicitudes solicitudes, ServicioCitas citas, ServicioAgendas agendas,
            ControlAcceso acceso) {
        this.solicitudes = solicitudes;
        this.citas = citas;
        this.agendas = agendas;
        this.acceso = acceso;
    }

    private String medico(String token) {
        return acceso.exigir(token, Permiso.CONSULTAR_ESTADO_SOLICITUDES).login();
    }

    @Override
    public List<SolicitudPrueba> misSolicitudes(String token) {
        return solicitudes.porMedico(medico(token));
    }

    @Override
    public List<SolicitudPrueba> misSolicitudes(String token, EstadoSolicitud estado) {
        return misSolicitudes(token).stream().filter(s -> s.getEstado() == estado).toList();
    }

    @Override
    public SituacionSolicitud situacion(String token, long idSolicitud) {
        String medico = medico(token);
        SolicitudPrueba solicitud = solicitudes.obtener(idSolicitud);
        if (!solicitud.getLoginMedico().equals(medico)) {
            throw new AccesoDenegadoException("La solicitud " + idSolicitud + " no es suya");
        }
        return SituacionSolicitud.de(solicitud, citas.deSolicitud(idSolicitud));
    }

    @Override
    public List<Agenda> misAgendas(String token, LocalDate desde, LocalDate hasta) {
        String medico = acceso.exigir(token, Permiso.CONSULTAR_DISPONIBILIDAD).login();
        return agendas.deMedico(medico, desde, hasta);
    }

    @Override
    public List<HuecoLibre> huecosLibres(String token, Modalidad modalidad, LocalDate desde, LocalDate hasta) {
        acceso.exigir(token, Permiso.CONSULTAR_DISPONIBILIDAD);
        return agendas.huecosLibres(modalidad, desde, hasta);
    }
}
