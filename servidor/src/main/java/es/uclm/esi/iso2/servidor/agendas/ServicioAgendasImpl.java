package es.uclm.esi.iso2.servidor.agendas;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

import es.uclm.esi.iso2.servidor.catalogo.Modalidad;
import es.uclm.esi.iso2.servidor.catalogo.ParametrosSistema;
import es.uclm.esi.iso2.servidor.comun.ReglaNegocioException;
import es.uclm.esi.iso2.servidor.comun.Repositorio;

public class ServicioAgendasImpl implements ServicioAgendas {

    private final Repositorio<Agenda, Long> repositorio;
    private final ParametrosSistema parametros;
    private final Clock reloj;
    private long secuencia;

    public ServicioAgendasImpl(Repositorio<Agenda, Long> repositorio, ParametrosSistema parametros, Clock reloj) {
        this.repositorio = repositorio;
        this.parametros = parametros;
        this.reloj = reloj;
    }

    @Override
    public Agenda crear(DatosAgenda datos) {
        Agenda agenda = new Agenda(secuencia + 1, datos);
        if (agenda.getFecha().isBefore(LocalDate.now(reloj))) {
            throw new ReglaNegocioException("No se pueden crear agendas en fechas pasadas");
        }
        if (!parametros.dentroDeHorario(agenda.getInicio(), agenda.getFin())) {
            throw new ReglaNegocioException("La agenda está fuera del horario del servicio ("
                    + parametros.apertura() + "-" + parametros.cierre() + ")");
        }
        for (Agenda otra : repositorio.todos()) {
            boolean solapa = otra.seSolapa(agenda.getFecha(), agenda.getInicio(), agenda.getFin());
            if (solapa && otra.getSala().equals(agenda.getSala())) {
                throw new ReglaNegocioException("La sala " + agenda.getSala() + " ya tiene agenda en ese horario");
            }
            if (solapa && otra.getLoginMedico().equals(agenda.getLoginMedico())) {
                throw new ReglaNegocioException("El médico " + agenda.getLoginMedico() + " ya tiene agenda en ese horario");
            }
        }
        secuencia++;
        return repositorio.guardar(agenda);
    }

    @Override
    public void eliminar(long idAgenda) {
        Agenda agenda = repositorio.obtener(idAgenda);
        if (agenda.huecosOcupados() > 0) {
            throw new ReglaNegocioException("La agenda " + idAgenda + " tiene citas; hay que reprogramarlas antes");
        }
        repositorio.eliminar(idAgenda);
    }

    @Override
    public Agenda obtener(long idAgenda) {
        return repositorio.obtener(idAgenda);
    }

    @Override
    public List<Agenda> delDia(LocalDate fecha) {
        return repositorio.filtrar(a -> a.getFecha().equals(fecha)).stream()
                .sorted(Comparator.comparing(Agenda::getInicio)).toList();
    }

    @Override
    public List<Agenda> todas() {
        return repositorio.todos();
    }

    @Override
    public List<HuecoLibre> huecosLibres(Modalidad modalidad, LocalDate desde, LocalDate hasta) {
        return repositorio.filtrar(a -> a.getModalidad() == modalidad && !a.getFecha().isBefore(desde)
                && !a.getFecha().isAfter(hasta)).stream()
                .flatMap(a -> a.huecosLibres().stream())
                .sorted(Comparator.comparing(HuecoLibre::momento))
                .toList();
    }

    @Override
    public void ocupar(long idAgenda, LocalTime hora, long idCita) {
        Agenda agenda = repositorio.obtener(idAgenda);
        agenda.ocupar(hora, idCita);
        repositorio.guardar(agenda);
    }

    @Override
    public void liberar(long idAgenda, LocalTime hora) {
        Agenda agenda = repositorio.obtener(idAgenda);
        agenda.liberar(hora);
        repositorio.guardar(agenda);
    }
}
