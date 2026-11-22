package es.uclm.esi.iso2.servidor.controlador;

import java.util.List;

import es.uclm.esi.iso2.servidor.api.ApiIntegracion;
import es.uclm.esi.iso2.servidor.seguridad.ControlAcceso;
import es.uclm.esi.iso2.servidor.seguridad.Permiso;
import es.uclm.esi.iso2.servidor.sincronizacion.EstadoSistema;
import es.uclm.esi.iso2.servidor.sincronizacion.ServicioIntegracion;
import es.uclm.esi.iso2.servidor.sincronizacion.Sincronizacion;

/** Las sincronizaciones mueven datos de pacientes e informes: se auditan (RNF-S2). */
public class ControladorIntegracion implements ApiIntegracion {

    private final ServicioIntegracion integracion;
    private final ControlAcceso acceso;

    public ControladorIntegracion(ServicioIntegracion integracion, ControlAcceso acceso) {
        this.integracion = integracion;
        this.acceso = acceso;
    }

    @Override
    public List<EstadoSistema> estado(String token) {
        acceso.exigir(token, Permiso.GESTIONAR_INTEGRACIONES, "estado de los sistemas");
        return integracion.estado();
    }

    @Override
    public Sincronizacion importarPacientes(String token) {
        acceso.exigir(token, Permiso.GESTIONAR_INTEGRACIONES, "importar pacientes de admisión");
        return integracion.importarPacientes();
    }

    @Override
    public Sincronizacion enviarInformesHce(String token) {
        acceso.exigir(token, Permiso.GESTIONAR_INTEGRACIONES, "enviar informes a la HCE");
        return integracion.enviarInformesHce();
    }

    @Override
    public List<Sincronizacion> sincronizaciones(String token) {
        acceso.exigir(token, Permiso.GESTIONAR_INTEGRACIONES, "sincronizaciones");
        return integracion.sincronizaciones();
    }
}
