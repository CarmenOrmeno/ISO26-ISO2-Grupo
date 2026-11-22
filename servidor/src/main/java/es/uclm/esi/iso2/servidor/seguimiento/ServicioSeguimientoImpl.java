package es.uclm.esi.iso2.servidor.seguimiento;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import es.uclm.esi.iso2.servidor.catalogo.ServicioCatalogo;
import es.uclm.esi.iso2.servidor.catalogo.TipoPrueba;
import es.uclm.esi.iso2.servidor.comun.AccesoDenegadoException;
import es.uclm.esi.iso2.servidor.comun.ReglaNegocioException;
import es.uclm.esi.iso2.servidor.comun.Repositorio;
import es.uclm.esi.iso2.servidor.notificaciones.Notificador;
import es.uclm.esi.iso2.servidor.notificaciones.TipoNotificacion;
import es.uclm.esi.iso2.servidor.pacientes.ServicioPacientes;
import es.uclm.esi.iso2.servidor.solicitudes.ServicioSolicitudes;
import es.uclm.esi.iso2.servidor.solicitudes.SolicitudPrueba;

public class ServicioSeguimientoImpl implements ServicioSeguimiento {

    private final Repositorio<Seguimiento, Long> repositorio;
    private final ServicioPacientes pacientes;
    private final ServicioCatalogo catalogo;
    private final ServicioSolicitudes solicitudes;
    private final Notificador notificador;
    private final Clock reloj;
    private long secuencia;

    public ServicioSeguimientoImpl(Repositorio<Seguimiento, Long> repositorio, ServicioPacientes pacientes,
            ServicioCatalogo catalogo, ServicioSolicitudes solicitudes, Notificador notificador, Clock reloj) {
        this.repositorio = repositorio;
        this.pacientes = pacientes;
        this.catalogo = catalogo;
        this.solicitudes = solicitudes;
        this.notificador = notificador;
        this.reloj = reloj;
    }

    @Override
    public Seguimiento prescribir(String loginMedico, DatosSeguimiento datos) {
        Seguimiento seguimiento = new Seguimiento(secuencia + 1, loginMedico, datos);
        pacientes.obtenerActivo(seguimiento.getIdPaciente());
        TipoPrueba tipo = catalogo.obtener(seguimiento.getCodigoTipo());
        if (!tipo.admiteSeguimiento()) {
            throw new ReglaNegocioException("El tipo de prueba " + tipo.codigo() + " no admite seguimiento");
        }
        if (seguimiento.getPrimeraRevision().isBefore(LocalDate.now(reloj))) {
            throw new ReglaNegocioException("La primera revisión no puede ser anterior a hoy");
        }
        secuencia++;
        repositorio.guardar(seguimiento);
        notificador.notificar(seguimiento.getIdPaciente(), TipoNotificacion.SEGUIMIENTO,
                "Se le ha prescrito un seguimiento de " + tipo.nombre() + " cada "
                        + seguimiento.getPeriodicidadMeses() + " meses; primera revisión el "
                        + seguimiento.getPrimeraRevision());
        return seguimiento;
    }

    @Override
    public void cancelar(long idSeguimiento, String loginMedico, String motivo) {
        Seguimiento s = repositorio.obtener(idSeguimiento);
        if (!s.getLoginMedico().equals(loginMedico)) {
            throw new AccesoDenegadoException("Solo el médico que lo prescribió puede cancelar el seguimiento");
        }
        s.cancelar(motivo);
        repositorio.guardar(s);
    }

    @Override
    public Seguimiento obtener(long idSeguimiento) {
        return repositorio.obtener(idSeguimiento);
    }

    @Override
    public List<Seguimiento> porMedico(String loginMedico) {
        return repositorio.filtrar(s -> s.getLoginMedico().equals(loginMedico));
    }

    @Override
    public List<Seguimiento> porPaciente(String idPaciente) {
        return repositorio.filtrar(s -> s.getIdPaciente().equals(idPaciente));
    }

    @Override
    public int generarRevisionesVencidas() {
        LocalDate hoy = LocalDate.now(reloj);
        int generadas = 0;
        for (Seguimiento s : repositorio.filtrar(s -> s.tieneRevisionVencida(hoy))) {
            SolicitudPrueba solicitud;
            try {
                solicitud = solicitudes.solicitarSeguimiento(s.getLoginMedico(), s.datosRevision(), s.getId());
            } catch (ReglaNegocioException aplazada) {
                // revisión anterior aún abierta o paciente de baja: se reintenta en la siguiente ejecución
                continue;
            }
            s.registrarRevision(solicitud.getId());
            repositorio.guardar(s);
            notificador.notificar(s.getIdPaciente(), TipoNotificacion.SEGUIMIENTO,
                    "Se ha generado la solicitud de su revisión " + s.revisionesGeneradas() + "/"
                            + s.getNumeroRevisiones() + " de " + s.getCodigoTipo());
            generadas++;
        }
        return generadas;
    }
}
