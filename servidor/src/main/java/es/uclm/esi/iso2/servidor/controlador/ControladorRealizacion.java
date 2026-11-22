package es.uclm.esi.iso2.servidor.controlador;

import java.util.List;

import es.uclm.esi.iso2.servidor.api.ApiRealizacion;
import es.uclm.esi.iso2.servidor.citas.Cita;
import es.uclm.esi.iso2.servidor.realizacion.Incidencia;
import es.uclm.esi.iso2.servidor.realizacion.ServicioRealizacion;
import es.uclm.esi.iso2.servidor.realizacion.TipoIncidencia;
import es.uclm.esi.iso2.servidor.seguridad.ControlAcceso;
import es.uclm.esi.iso2.servidor.seguridad.Permiso;

public class ControladorRealizacion implements ApiRealizacion {

    private final ServicioRealizacion servicio;
    private final ControlAcceso acceso;

    public ControladorRealizacion(ServicioRealizacion servicio, ControlAcceso acceso) {
        this.servicio = servicio;
        this.acceso = acceso;
    }

    @Override
    public Cita registrarRealizacion(String token, long idCita, int numeroImagenes, String observaciones) {
        String medico = acceso.exigir(token, Permiso.REGISTRAR_REALIZACION).login();
        return servicio.registrarRealizacion(idCita, medico, numeroImagenes, observaciones);
    }

    @Override
    public Incidencia registrarIncidencia(String token, long idCita, TipoIncidencia tipo, String descripcion) {
        String medico = acceso.exigir(token, Permiso.REGISTRAR_REALIZACION).login();
        return servicio.registrarIncidencia(idCita, medico, tipo, descripcion);
    }

    @Override
    public List<Incidencia> incidenciasDeCita(String token, long idCita) {
        acceso.exigir(token, Permiso.REGISTRAR_REALIZACION);
        return servicio.incidenciasDeCita(idCita);
    }
}
