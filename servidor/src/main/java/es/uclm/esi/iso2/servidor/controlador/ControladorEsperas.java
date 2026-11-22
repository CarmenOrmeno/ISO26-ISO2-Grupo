package es.uclm.esi.iso2.servidor.controlador;

import java.util.List;

import es.uclm.esi.iso2.servidor.api.ApiEsperas;
import es.uclm.esi.iso2.servidor.catalogo.Modalidad;
import es.uclm.esi.iso2.servidor.catalogo.Prioridad;
import es.uclm.esi.iso2.servidor.citas.Cita;
import es.uclm.esi.iso2.servidor.esperas.EntradaEspera;
import es.uclm.esi.iso2.servidor.esperas.ServicioEsperas;
import es.uclm.esi.iso2.servidor.seguridad.ControlAcceso;
import es.uclm.esi.iso2.servidor.seguridad.Permiso;

public class ControladorEsperas implements ApiEsperas {

    private final ServicioEsperas esperas;
    private final ControlAcceso acceso;

    public ControladorEsperas(ServicioEsperas esperas, ControlAcceso acceso) {
        this.esperas = esperas;
        this.acceso = acceso;
    }

    private void exigirPermiso(String token) {
        acceso.exigir(token, Permiso.GESTIONAR_LISTAS_ESPERA);
    }

    @Override
    public List<EntradaEspera> listaDeEspera(String token) {
        exigirPermiso(token);
        return esperas.listaDeEspera();
    }

    @Override
    public List<EntradaEspera> listaDeEspera(String token, Modalidad modalidad) {
        exigirPermiso(token);
        return esperas.listaDeEspera(modalidad);
    }

    @Override
    public void cambiarPrioridad(String token, long idSolicitud, Prioridad prioridad) {
        exigirPermiso(token);
        esperas.cambiarPrioridad(idSolicitud, prioridad);
    }

    @Override
    public Cita citarEnPrimerHueco(String token, long idSolicitud) {
        exigirPermiso(token);
        return esperas.citarEnPrimerHueco(idSolicitud);
    }

    @Override
    public int rellenarHuecos(String token, Modalidad modalidad) {
        exigirPermiso(token);
        return esperas.rellenarHuecos(modalidad);
    }
}
