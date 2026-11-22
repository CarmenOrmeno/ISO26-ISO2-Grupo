package es.uclm.esi.iso2.servidor.controlador;

import java.time.LocalDate;
import java.util.List;

import es.uclm.esi.iso2.servidor.api.ApiHistorial;
import es.uclm.esi.iso2.servidor.historial.EntradaHistorial;
import es.uclm.esi.iso2.servidor.historial.ServicioHistorial;
import es.uclm.esi.iso2.servidor.informes.Informe;
import es.uclm.esi.iso2.servidor.seguridad.ControlAcceso;
import es.uclm.esi.iso2.servidor.seguridad.Permiso;

/** El paciente solo ve su propio historial (RNF-S3). */
public class ControladorHistorial implements ApiHistorial {

    private final ServicioHistorial historial;
    private final ControlAcceso acceso;

    public ControladorHistorial(ServicioHistorial historial, ControlAcceso acceso) {
        this.historial = historial;
        this.acceso = acceso;
    }

    private String paciente(String token) {
        return acceso.exigir(token, Permiso.CONSULTAR_MI_HISTORIAL).exigirPaciente();
    }

    @Override
    public List<EntradaHistorial> miHistorial(String token) {
        return historial.historial(paciente(token));
    }

    @Override
    public List<EntradaHistorial> miHistorial(String token, LocalDate desde, LocalDate hasta) {
        return historial.historial(paciente(token), desde, hasta);
    }

    @Override
    public Informe miInforme(String token, long idSolicitud) {
        return historial.informe(paciente(token), idSolicitud);
    }
}
