package es.uclm.esi.iso2.servidor.controlador;

import java.time.LocalDate;

import es.uclm.esi.iso2.servidor.api.ApiEstadisticasMedico;
import es.uclm.esi.iso2.servidor.estadisticas.EstadisticasMedico;
import es.uclm.esi.iso2.servidor.estadisticas.Periodo;
import es.uclm.esi.iso2.servidor.estadisticas.ServicioEstadisticasMedico;
import es.uclm.esi.iso2.servidor.seguridad.ControlAcceso;
import es.uclm.esi.iso2.servidor.seguridad.Permiso;

/** Cada médico solo ve las estadísticas de sus propias solicitudes. */
public class ControladorEstadisticasMedico implements ApiEstadisticasMedico {

    private final ServicioEstadisticasMedico estadisticas;
    private final ControlAcceso acceso;

    public ControladorEstadisticasMedico(ServicioEstadisticasMedico estadisticas, ControlAcceso acceso) {
        this.estadisticas = estadisticas;
        this.acceso = acceso;
    }

    @Override
    public EstadisticasMedico misEstadisticas(String token, LocalDate desde, LocalDate hasta) {
        String medico = acceso.exigir(token, Permiso.CONSULTAR_MIS_ESTADISTICAS).login();
        return estadisticas.estadisticas(medico, new Periodo(desde, hasta));
    }
}
