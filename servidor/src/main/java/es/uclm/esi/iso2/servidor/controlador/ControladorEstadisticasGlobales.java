package es.uclm.esi.iso2.servidor.controlador;

import java.time.LocalDate;

import es.uclm.esi.iso2.servidor.api.ApiEstadisticasGlobales;
import es.uclm.esi.iso2.servidor.estadisticas.EstadisticasGlobales;
import es.uclm.esi.iso2.servidor.estadisticas.Periodo;
import es.uclm.esi.iso2.servidor.estadisticas.ServicioEstadisticasGlobales;
import es.uclm.esi.iso2.servidor.seguridad.ControlAcceso;
import es.uclm.esi.iso2.servidor.seguridad.Permiso;

public class ControladorEstadisticasGlobales implements ApiEstadisticasGlobales {

    private final ServicioEstadisticasGlobales estadisticas;
    private final ControlAcceso acceso;

    public ControladorEstadisticasGlobales(ServicioEstadisticasGlobales estadisticas, ControlAcceso acceso) {
        this.estadisticas = estadisticas;
        this.acceso = acceso;
    }

    @Override
    public EstadisticasGlobales estadisticas(String token, LocalDate desde, LocalDate hasta) {
        acceso.exigir(token, Permiso.CONSULTAR_ESTADISTICAS_GLOBALES);
        return estadisticas.estadisticas(new Periodo(desde, hasta));
    }
}
