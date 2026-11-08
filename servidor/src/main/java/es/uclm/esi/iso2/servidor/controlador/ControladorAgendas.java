package es.uclm.esi.iso2.servidor.controlador;

import java.time.LocalDate;
import java.util.List;

import es.uclm.esi.iso2.servidor.agendas.Agenda;
import es.uclm.esi.iso2.servidor.agendas.DatosAgenda;
import es.uclm.esi.iso2.servidor.agendas.HuecoLibre;
import es.uclm.esi.iso2.servidor.agendas.ServicioAgendas;
import es.uclm.esi.iso2.servidor.api.ApiAgendas;
import es.uclm.esi.iso2.servidor.catalogo.Modalidad;
import es.uclm.esi.iso2.servidor.seguridad.ControlAcceso;
import es.uclm.esi.iso2.servidor.seguridad.Permiso;

public class ControladorAgendas implements ApiAgendas {

    private final ServicioAgendas servicio;
    private final ControlAcceso acceso;

    public ControladorAgendas(ServicioAgendas servicio, ControlAcceso acceso) {
        this.servicio = servicio;
        this.acceso = acceso;
    }

    @Override
    public Agenda crear(String token, DatosAgenda datos) {
        acceso.exigir(token, Permiso.GESTIONAR_AGENDAS);
        return servicio.crear(datos);
    }

    @Override
    public void eliminar(String token, long idAgenda) {
        acceso.exigir(token, Permiso.GESTIONAR_AGENDAS);
        servicio.eliminar(idAgenda);
    }

    @Override
    public Agenda obtener(String token, long idAgenda) {
        acceso.exigir(token, Permiso.CONSULTAR_AGENDAS);
        return servicio.obtener(idAgenda);
    }

    @Override
    public List<Agenda> delDia(String token, LocalDate fecha) {
        acceso.exigir(token, Permiso.CONSULTAR_AGENDAS);
        return servicio.delDia(fecha);
    }

    @Override
    public List<HuecoLibre> huecosLibres(String token, Modalidad modalidad, LocalDate desde, LocalDate hasta) {
        acceso.exigir(token, Permiso.CONSULTAR_AGENDAS);
        return servicio.huecosLibres(modalidad, desde, hasta);
    }
}
