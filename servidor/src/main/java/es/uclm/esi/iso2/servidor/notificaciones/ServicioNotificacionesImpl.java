package es.uclm.esi.iso2.servidor.notificaciones;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import es.uclm.esi.iso2.servidor.comun.AccesoDenegadoException;
import es.uclm.esi.iso2.servidor.comun.Repositorio;

/** Buzón en la aplicación: guarda cada aviso para que el paciente lo consulte. */
public class ServicioNotificacionesImpl implements ServicioNotificaciones {

    private final Repositorio<Notificacion, Long> repositorio;
    private final Clock reloj;
    private long secuencia;

    public ServicioNotificacionesImpl(Repositorio<Notificacion, Long> repositorio, Clock reloj) {
        this.repositorio = repositorio;
        this.reloj = reloj;
    }

    @Override
    public void notificar(String idPaciente, TipoNotificacion tipo, String mensaje) {
        Notificacion n = new Notificacion(secuencia + 1, idPaciente, tipo, mensaje, LocalDateTime.now(reloj));
        secuencia++;
        repositorio.guardar(n);
    }

    @Override
    public List<Notificacion> dePaciente(String idPaciente) {
        return repositorio.filtrar(n -> n.getIdPaciente().equals(idPaciente)).stream()
                .sorted(Comparator.comparing(Notificacion::getId).reversed()).toList();
    }

    @Override
    public long noLeidas(String idPaciente) {
        return dePaciente(idPaciente).stream().filter(n -> !n.isLeida()).count();
    }

    @Override
    public void marcarLeida(long idNotificacion, String idPaciente) {
        Notificacion n = repositorio.obtener(idNotificacion);
        if (!n.getIdPaciente().equals(idPaciente)) {
            throw new AccesoDenegadoException("La notificación " + idNotificacion + " no es suya");
        }
        n.marcarLeida();
        repositorio.guardar(n);
    }
}
