package es.uclm.esi.iso2.servidor.controlador;

import java.time.LocalDate;
import java.util.List;

import es.uclm.esi.iso2.servidor.api.ApiAuditoria;
import es.uclm.esi.iso2.servidor.auditoria.RegistroAuditoria;
import es.uclm.esi.iso2.servidor.auditoria.ServicioAuditoria;
import es.uclm.esi.iso2.servidor.seguridad.ControlAcceso;
import es.uclm.esi.iso2.servidor.seguridad.Permiso;

public class ControladorAuditoria implements ApiAuditoria {

    private final ServicioAuditoria auditoria;
    private final ControlAcceso acceso;

    public ControladorAuditoria(ServicioAuditoria auditoria, ControlAcceso acceso) {
        this.auditoria = auditoria;
        this.acceso = acceso;
    }

    @Override
    public List<RegistroAuditoria> registros(String token, LocalDate desde, LocalDate hasta) {
        acceso.exigir(token, Permiso.CONSULTAR_AUDITORIA);
        return auditoria.registros(desde, hasta);
    }

    @Override
    public List<RegistroAuditoria> registrosDeUsuario(String token, String login, LocalDate desde, LocalDate hasta) {
        acceso.exigir(token, Permiso.CONSULTAR_AUDITORIA);
        return auditoria.deUsuario(login, desde, hasta);
    }

    @Override
    public List<RegistroAuditoria> accesosDenegados(String token, LocalDate desde, LocalDate hasta) {
        acceso.exigir(token, Permiso.CONSULTAR_AUDITORIA);
        return auditoria.denegados(desde, hasta);
    }

    @Override
    public List<RegistroAuditoria> accesosARecurso(String token, String texto, LocalDate desde, LocalDate hasta) {
        acceso.exigir(token, Permiso.CONSULTAR_AUDITORIA);
        return auditoria.sobreRecurso(texto, desde, hasta);
    }
}
