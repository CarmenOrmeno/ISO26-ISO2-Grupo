package es.uclm.esi.iso2.servidor.solicitudes;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import es.uclm.esi.iso2.servidor.catalogo.ServicioCatalogo;
import es.uclm.esi.iso2.servidor.comun.AccesoDenegadoException;
import es.uclm.esi.iso2.servidor.comun.ReglaNegocioException;
import es.uclm.esi.iso2.servidor.comun.Repositorio;
import es.uclm.esi.iso2.servidor.pacientes.ServicioPacientes;

public class ServicioSolicitudesImpl implements ServicioSolicitudes {

    private final Repositorio<SolicitudPrueba, Long> repositorio;
    private final ServicioPacientes pacientes;
    private final ServicioCatalogo catalogo;
    private final Clock reloj;
    private long secuencia;

    public ServicioSolicitudesImpl(Repositorio<SolicitudPrueba, Long> repositorio, ServicioPacientes pacientes,
            ServicioCatalogo catalogo, Clock reloj) {
        this.repositorio = repositorio;
        this.pacientes = pacientes;
        this.catalogo = catalogo;
        this.reloj = reloj;
    }

    @Override
    public SolicitudPrueba solicitar(String loginMedico, DatosSolicitud datos) {
        return registrar(loginMedico, datos, null);
    }

    /** Crea y guarda una solicitud validando paciente, tipo y duplicados. */
    protected SolicitudPrueba registrar(String loginMedico, DatosSolicitud datos, Long idSeguimiento) {
        SolicitudPrueba solicitud = new SolicitudPrueba(secuencia + 1, loginMedico, datos,
                LocalDateTime.now(reloj), idSeguimiento);
        pacientes.obtenerActivo(solicitud.getIdPaciente());
        catalogo.obtener(solicitud.getCodigoTipo());
        boolean duplicada = repositorio.filtrar(s -> s.getIdPaciente().equals(solicitud.getIdPaciente())
                && s.getCodigoTipo().equals(solicitud.getCodigoTipo()) && s.getEstado().abierta()).size() > 0;
        if (duplicada) {
            throw new ReglaNegocioException("El paciente ya tiene una solicitud abierta de " + solicitud.getCodigoTipo());
        }
        secuencia++;
        return repositorio.guardar(solicitud);
    }

    @Override
    public void cancelar(long idSolicitud, String loginMedico, String motivo) {
        SolicitudPrueba s = repositorio.obtener(idSolicitud);
        if (!s.getLoginMedico().equals(loginMedico)) {
            throw new AccesoDenegadoException("Solo el médico solicitante puede cancelar la solicitud");
        }
        s.cancelar(motivo);
        repositorio.guardar(s);
    }

    @Override
    public SolicitudPrueba obtener(long idSolicitud) {
        return repositorio.obtener(idSolicitud);
    }

    @Override
    public List<SolicitudPrueba> porMedico(String loginMedico) {
        return repositorio.filtrar(s -> s.getLoginMedico().equals(loginMedico));
    }

    @Override
    public List<SolicitudPrueba> porPaciente(String idPaciente) {
        return repositorio.filtrar(s -> s.getIdPaciente().equals(idPaciente));
    }

    @Override
    public List<SolicitudPrueba> pendientes() {
        return repositorio.filtrar(s -> s.getEstado() == EstadoSolicitud.SOLICITADA);
    }

    @Override
    public List<SolicitudPrueba> todas() {
        return repositorio.todos();
    }
}
