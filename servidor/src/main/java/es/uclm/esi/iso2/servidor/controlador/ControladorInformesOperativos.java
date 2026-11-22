package es.uclm.esi.iso2.servidor.controlador;

import java.time.LocalDate;

import es.uclm.esi.iso2.servidor.api.ApiInformesOperativos;
import es.uclm.esi.iso2.servidor.estadisticas.InformeOperativo;
import es.uclm.esi.iso2.servidor.estadisticas.Periodo;
import es.uclm.esi.iso2.servidor.estadisticas.ServicioInformesOperativos;
import es.uclm.esi.iso2.servidor.seguridad.ControlAcceso;
import es.uclm.esi.iso2.servidor.seguridad.Permiso;

public class ControladorInformesOperativos implements ApiInformesOperativos {

    private final ServicioInformesOperativos informes;
    private final ControlAcceso acceso;

    public ControladorInformesOperativos(ServicioInformesOperativos informes, ControlAcceso acceso) {
        this.informes = informes;
        this.acceso = acceso;
    }

    @Override
    public InformeOperativo generar(String token, LocalDate desde, LocalDate hasta) {
        acceso.exigir(token, Permiso.GENERAR_INFORMES_OPERATIVOS);
        return informes.generar(new Periodo(desde, hasta));
    }
}
