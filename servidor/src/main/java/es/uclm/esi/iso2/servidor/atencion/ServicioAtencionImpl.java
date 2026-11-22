package es.uclm.esi.iso2.servidor.atencion;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import es.uclm.esi.iso2.servidor.comun.AccesoDenegadoException;
import es.uclm.esi.iso2.servidor.comun.ReglaNegocioException;
import es.uclm.esi.iso2.servidor.comun.Repositorio;
import es.uclm.esi.iso2.servidor.comun.Secuencia;
import es.uclm.esi.iso2.servidor.solicitudes.EstadoSolicitud;
import es.uclm.esi.iso2.servidor.solicitudes.ServicioSolicitudes;
import es.uclm.esi.iso2.servidor.solicitudes.SolicitudPrueba;

public class ServicioAtencionImpl implements ServicioAtencion {

    private final Repositorio<IncidenciaPaciente, Long> incidencias;
    private final Repositorio<Encuesta, Long> encuestas;
    private final ServicioSolicitudes solicitudes;
    private final Clock reloj;
    private final Secuencia secuencia;

    public ServicioAtencionImpl(Repositorio<IncidenciaPaciente, Long> incidencias, Repositorio<Encuesta, Long> encuestas,
            ServicioSolicitudes solicitudes, Clock reloj) {
        this.incidencias = incidencias;
        this.encuestas = encuestas;
        this.solicitudes = solicitudes;
        this.reloj = reloj;
        this.secuencia = Secuencia.continuando(incidencias, IncidenciaPaciente::getId);
    }

    @Override
    public IncidenciaPaciente comunicarIncidencia(String idPaciente, CategoriaIncidencia categoria,
            String descripcion, Long idSolicitud) {
        if (idSolicitud != null) {
            solicitudDe(idPaciente, idSolicitud);
        }
        IncidenciaPaciente incidencia = new IncidenciaPaciente(secuencia.siguiente(), idPaciente, categoria,
                descripcion, idSolicitud, LocalDateTime.now(reloj));
        secuencia.confirmar();
        return incidencias.guardar(incidencia);
    }

    @Override
    public List<IncidenciaPaciente> incidenciasDe(String idPaciente) {
        return incidencias.filtrar(i -> i.getIdPaciente().equals(idPaciente)).stream()
                .sorted(Comparator.comparing(IncidenciaPaciente::getId).reversed())
                .toList();
    }

    @Override
    public List<IncidenciaPaciente> incidenciasAbiertas() {
        return incidencias.filtrar(i -> i.getEstado() == EstadoIncidencia.ABIERTA).stream()
                .sorted(Comparator.comparing(IncidenciaPaciente::getId))
                .toList();
    }

    @Override
    public IncidenciaPaciente responderIncidencia(long idIncidencia, String login, String respuesta) {
        IncidenciaPaciente incidencia = incidencias.obtener(idIncidencia);
        incidencia.responder(respuesta, login, LocalDateTime.now(reloj));
        return incidencias.guardar(incidencia);
    }

    @Override
    public List<SolicitudPrueba> encuestasPendientes(String idPaciente) {
        return solicitudes.porPaciente(idPaciente).stream()
                .filter(ServicioAtencionImpl::realizada)
                .filter(s -> !encuestas.existe(s.getId()))
                .toList();
    }

    @Override
    public Encuesta responderEncuesta(String idPaciente, long idSolicitud, int puntuacion, String comentario) {
        SolicitudPrueba solicitud = solicitudDe(idPaciente, idSolicitud);
        if (!realizada(solicitud)) {
            throw new ReglaNegocioException("La prueba " + idSolicitud + " aún no se ha realizado");
        }
        if (encuestas.existe(idSolicitud)) {
            throw new ReglaNegocioException("La encuesta de la prueba " + idSolicitud + " ya está respondida");
        }
        return encuestas.guardar(new Encuesta(idSolicitud, idPaciente, puntuacion, comentario,
                LocalDateTime.now(reloj)));
    }

    @Override
    public List<Encuesta> encuestasDe(String idPaciente) {
        return encuestas.filtrar(e -> e.idPaciente().equals(idPaciente));
    }

    @Override
    public List<Encuesta> todasLasEncuestas() {
        return encuestas.todos();
    }

    /** La prueba existe (si no, NoEncontradoException) y es del paciente (RNF-S3). */
    private SolicitudPrueba solicitudDe(String idPaciente, long idSolicitud) {
        SolicitudPrueba solicitud = solicitudes.obtener(idSolicitud);
        if (!solicitud.getIdPaciente().equals(idPaciente)) {
            throw new AccesoDenegadoException("La prueba " + idSolicitud + " no es suya");
        }
        return solicitud;
    }

    private static boolean realizada(SolicitudPrueba s) {
        return s.getEstado() == EstadoSolicitud.REALIZADA || s.getEstado() == EstadoSolicitud.INFORMADA;
    }
}
