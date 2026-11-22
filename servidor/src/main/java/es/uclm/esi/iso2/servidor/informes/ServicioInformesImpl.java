package es.uclm.esi.iso2.servidor.informes;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import es.uclm.esi.iso2.servidor.agendas.ServicioAgendas;
import es.uclm.esi.iso2.servidor.citas.ServicioCitas;
import es.uclm.esi.iso2.servidor.comun.ReglaNegocioException;
import es.uclm.esi.iso2.servidor.comun.Repositorio;
import es.uclm.esi.iso2.servidor.integracion.ImagenDicom;
import es.uclm.esi.iso2.servidor.integracion.SistemaPacs;
import es.uclm.esi.iso2.servidor.notificaciones.Notificador;
import es.uclm.esi.iso2.servidor.notificaciones.TipoNotificacion;
import es.uclm.esi.iso2.servidor.solicitudes.EstadoSolicitud;
import es.uclm.esi.iso2.servidor.solicitudes.ServicioSolicitudes;
import es.uclm.esi.iso2.servidor.solicitudes.SolicitudPrueba;

public class ServicioInformesImpl implements ServicioInformes {

    private final Repositorio<Informe, Long> repositorio;
    private final ServicioSolicitudes solicitudes;
    private final ServicioCitas citas;
    private final ServicioAgendas agendas;
    private final SistemaPacs pacs;
    private final Notificador notificador;
    private final Clock reloj;
    private long secuencia;

    public ServicioInformesImpl(Repositorio<Informe, Long> repositorio, ServicioSolicitudes solicitudes,
            ServicioCitas citas, ServicioAgendas agendas, SistemaPacs pacs, Notificador notificador, Clock reloj) {
        this.repositorio = repositorio;
        this.solicitudes = solicitudes;
        this.citas = citas;
        this.agendas = agendas;
        this.pacs = pacs;
        this.notificador = notificador;
        this.reloj = reloj;
    }

    @Override
    public Informe crearInforme(long idSolicitud, String loginMedico, String hallazgos, String conclusion) {
        SolicitudPrueba s = solicitudes.obtener(idSolicitud);
        if (s.getEstado() != EstadoSolicitud.REALIZADA) {
            throw new ReglaNegocioException("Solo se informa una prueba realizada y sin informe (está " + s.getEstado() + ")");
        }
        Informe informe = new Informe(secuencia + 1, idSolicitud, s.getIdPaciente(), loginMedico, hallazgos,
                conclusion, LocalDateTime.now(reloj));
        secuencia++;
        repositorio.guardar(informe);
        solicitudes.marcarInformada(idSolicitud);
        notificador.notificar(s.getIdPaciente(), TipoNotificacion.INFORME_DISPONIBLE,
                "Ya está disponible el informe de su prueba " + s.getCodigoTipo());
        return informe;
    }

    @Override
    public Optional<Informe> informeDeSolicitud(long idSolicitud) {
        return repositorio.filtrar(i -> i.idSolicitud() == idSolicitud).stream().findFirst();
    }

    @Override
    public List<Informe> informesDePaciente(String idPaciente) {
        return repositorio.filtrar(i -> i.idPaciente().equals(idPaciente));
    }

    @Override
    public List<ImagenDicom> imagenes(long idSolicitud) {
        solicitudes.obtener(idSolicitud);
        return pacs.imagenes(idSolicitud);
    }

    @Override
    public boolean esPacienteDe(String loginMedico, String idPaciente) {
        boolean solicitante = solicitudes.porPaciente(idPaciente).stream()
                .anyMatch(s -> s.getLoginMedico().equals(loginMedico));
        boolean enSuAgenda = citas.dePaciente(idPaciente).stream()
                .anyMatch(c -> agendas.obtener(c.getIdAgenda()).getLoginMedico().equals(loginMedico));
        return solicitante || enSuAgenda;
    }

    @Override
    public List<Informe> todos() {
        return repositorio.todos();
    }
}
