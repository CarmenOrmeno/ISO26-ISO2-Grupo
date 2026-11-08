package es.uclm.esi.iso2.servidor.controlador;

import java.time.LocalTime;
import java.util.List;

import es.uclm.esi.iso2.servidor.api.ApiCatalogo;
import es.uclm.esi.iso2.servidor.catalogo.Modalidad;
import es.uclm.esi.iso2.servidor.catalogo.Prioridad;
import es.uclm.esi.iso2.servidor.catalogo.ServicioCatalogo;
import es.uclm.esi.iso2.servidor.catalogo.TipoPrueba;
import es.uclm.esi.iso2.servidor.seguridad.ControlAcceso;
import es.uclm.esi.iso2.servidor.seguridad.Permiso;

public class ControladorCatalogo implements ApiCatalogo {

    private final ServicioCatalogo servicio;
    private final ControlAcceso acceso;

    public ControladorCatalogo(ServicioCatalogo servicio, ControlAcceso acceso) {
        this.servicio = servicio;
        this.acceso = acceso;
    }

    @Override
    public List<TipoPrueba> listar(String token) {
        acceso.exigir(token, Permiso.CONSULTAR_CATALOGO);
        return servicio.listar();
    }

    @Override
    public List<TipoPrueba> listarPorModalidad(String token, Modalidad modalidad) {
        acceso.exigir(token, Permiso.CONSULTAR_CATALOGO);
        return servicio.listarPorModalidad(modalidad);
    }

    @Override
    public TipoPrueba obtener(String token, String codigo) {
        acceso.exigir(token, Permiso.CONSULTAR_CATALOGO);
        return servicio.obtener(codigo);
    }

    @Override
    public TipoPrueba alta(String token, TipoPrueba tipo) {
        acceso.exigir(token, Permiso.CONFIGURAR_SISTEMA);
        return servicio.alta(tipo);
    }

    @Override
    public TipoPrueba modificar(String token, TipoPrueba tipo) {
        acceso.exigir(token, Permiso.CONFIGURAR_SISTEMA);
        return servicio.modificar(tipo);
    }

    @Override
    public void baja(String token, String codigo) {
        acceso.exigir(token, Permiso.CONFIGURAR_SISTEMA);
        servicio.baja(codigo);
    }

    @Override
    public String parametros(String token) {
        acceso.exigir(token, Permiso.CONSULTAR_CATALOGO);
        return servicio.parametros().toString();
    }

    @Override
    public void cambiarHorario(String token, LocalTime apertura, LocalTime cierre) {
        acceso.exigir(token, Permiso.CONFIGURAR_SISTEMA);
        servicio.parametros().cambiarHorario(apertura, cierre);
    }

    @Override
    public void cambiarPlazo(String token, Prioridad prioridad, int dias) {
        acceso.exigir(token, Permiso.CONFIGURAR_SISTEMA);
        servicio.parametros().cambiarPlazo(prioridad, dias);
    }

    @Override
    public void cambiarHorasRecordatorio(String token, int horas) {
        acceso.exigir(token, Permiso.CONFIGURAR_SISTEMA);
        servicio.parametros().cambiarHorasRecordatorio(horas);
    }
}
