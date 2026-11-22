package es.uclm.esi.iso2.servidor.controlador;

import java.util.List;

import es.uclm.esi.iso2.servidor.api.ApiMisSeguimientos;
import es.uclm.esi.iso2.servidor.comun.AccesoDenegadoException;
import es.uclm.esi.iso2.servidor.seguimiento.Seguimiento;
import es.uclm.esi.iso2.servidor.seguimiento.ServicioSeguimiento;
import es.uclm.esi.iso2.servidor.seguridad.ControlAcceso;
import es.uclm.esi.iso2.servidor.seguridad.Permiso;
import es.uclm.esi.iso2.servidor.solicitudes.ServicioSolicitudes;
import es.uclm.esi.iso2.servidor.solicitudes.SolicitudPrueba;

/** El paciente solo ve sus propios seguimientos (RNF-S3). */
public class ControladorMisSeguimientos implements ApiMisSeguimientos {

    private final ServicioSeguimiento seguimientos;
    private final ServicioSolicitudes solicitudes;
    private final ControlAcceso acceso;

    public ControladorMisSeguimientos(ServicioSeguimiento seguimientos, ServicioSolicitudes solicitudes,
            ControlAcceso acceso) {
        this.seguimientos = seguimientos;
        this.solicitudes = solicitudes;
        this.acceso = acceso;
    }

    private String paciente(String token) {
        return acceso.exigir(token, Permiso.CONSULTAR_MIS_SEGUIMIENTOS).exigirPaciente();
    }

    @Override
    public List<Seguimiento> misSeguimientos(String token) {
        return seguimientos.porPaciente(paciente(token));
    }

    @Override
    public List<SolicitudPrueba> revisiones(String token, long idSeguimiento) {
        String paciente = paciente(token);
        Seguimiento s = seguimientos.obtener(idSeguimiento);
        if (!s.getIdPaciente().equals(paciente)) {
            throw new AccesoDenegadoException("El seguimiento " + idSeguimiento + " no es suyo");
        }
        return s.getSolicitudes().stream().map(solicitudes::obtener).toList();
    }
}
