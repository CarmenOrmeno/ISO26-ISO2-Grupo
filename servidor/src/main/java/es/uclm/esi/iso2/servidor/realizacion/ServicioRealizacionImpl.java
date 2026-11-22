package es.uclm.esi.iso2.servidor.realizacion;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import es.uclm.esi.iso2.servidor.agendas.ServicioAgendas;
import es.uclm.esi.iso2.servidor.citas.Cita;
import es.uclm.esi.iso2.servidor.citas.ServicioCitas;
import es.uclm.esi.iso2.servidor.comun.AccesoDenegadoException;
import es.uclm.esi.iso2.servidor.comun.ReglaNegocioException;
import es.uclm.esi.iso2.servidor.comun.Repositorio;
import es.uclm.esi.iso2.servidor.integracion.SistemaPacs;
import es.uclm.esi.iso2.servidor.solicitudes.ServicioSolicitudes;

public class ServicioRealizacionImpl implements ServicioRealizacion {

    private final ServicioCitas citas;
    private final ServicioSolicitudes solicitudes;
    private final ServicioAgendas agendas;
    private final SistemaPacs pacs;
    private final Repositorio<Incidencia, Long> incidencias;
    private final Clock reloj;
    private long secuencia;

    public ServicioRealizacionImpl(ServicioCitas citas, ServicioSolicitudes solicitudes, ServicioAgendas agendas,
            SistemaPacs pacs, Repositorio<Incidencia, Long> incidencias, Clock reloj) {
        this.citas = citas;
        this.solicitudes = solicitudes;
        this.agendas = agendas;
        this.pacs = pacs;
        this.incidencias = incidencias;
        this.reloj = reloj;
    }

    @Override
    public Cita registrarRealizacion(long idCita, String loginMedico, int numeroImagenes, String observaciones) {
        Cita cita = citaDelMedico(idCita, loginMedico);
        LocalDateTime ahora = LocalDateTime.now(reloj);
        if (ahora.isBefore(cita.getFechaHora())) {
            throw new ReglaNegocioException("La cita " + idCita + " todavía no ha empezado (" + cita.getFechaHora() + ")");
        }
        String modalidad = agendas.obtener(cita.getIdAgenda()).getModalidad().name();
        pacs.registrarEstudio(cita.getIdSolicitud(), cita.getIdPaciente(), modalidad, numeroImagenes);
        Cita realizada = citas.marcarRealizada(idCita);
        solicitudes.registrarRealizacion(cita.getIdSolicitud(), ahora, observaciones);
        return realizada;
    }

    @Override
    public Incidencia registrarIncidencia(long idCita, String loginMedico, TipoIncidencia tipo, String descripcion) {
        citaDelMedico(idCita, loginMedico);
        Incidencia incidencia = new Incidencia(secuencia + 1, idCita, tipo, descripcion, loginMedico,
                LocalDateTime.now(reloj));
        secuencia++;
        return incidencias.guardar(incidencia);
    }

    @Override
    public List<Incidencia> incidenciasDeCita(long idCita) {
        return incidencias.filtrar(i -> i.idCita() == idCita);
    }

    @Override
    public List<Incidencia> todasLasIncidencias() {
        return incidencias.todos();
    }

    /** Solo el médico de la agenda en la que está la cita puede registrar su realización. */
    private Cita citaDelMedico(long idCita, String loginMedico) {
        Cita cita = citas.obtener(idCita);
        if (!agendas.obtener(cita.getIdAgenda()).getLoginMedico().equals(loginMedico)) {
            throw new AccesoDenegadoException("La cita " + idCita + " no está en una agenda de " + loginMedico);
        }
        return cita;
    }
}
