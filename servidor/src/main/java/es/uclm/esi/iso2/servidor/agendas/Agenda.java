package es.uclm.esi.iso2.servidor.agendas;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.uclm.esi.iso2.servidor.catalogo.Modalidad;
import es.uclm.esi.iso2.servidor.comun.NoEncontradoException;
import es.uclm.esi.iso2.servidor.comun.Validaciones;

/** Agenda de trabajo de una sala/equipo y un médico en un día, dividida en huecos iguales (CU05). */
public class Agenda {

    public static final int MINUTOS_HUECO_MIN = 5;
    public static final int MINUTOS_HUECO_MAX = 120;

    private final long id;
    private final LocalDate fecha;
    private final String sala;
    private final Modalidad modalidad;
    private final String loginMedico;
    private final LocalTime inicio;
    private final LocalTime fin;
    private final int minutosPorHueco;
    private final List<Hueco> huecos = new ArrayList<>();

    public Agenda(long id, DatosAgenda d) {
        Validaciones.obligatorio(d, "datos");
        this.id = id;
        this.fecha = Validaciones.obligatorio(d.fecha(), "fecha");
        this.sala = Validaciones.texto(d.sala(), "sala");
        this.modalidad = Validaciones.obligatorio(d.modalidad(), "modalidad");
        this.loginMedico = Validaciones.texto(d.loginMedico(), "médico");
        this.inicio = Validaciones.obligatorio(d.inicio(), "hora de inicio");
        this.fin = Validaciones.obligatorio(d.fin(), "hora de fin");
        this.minutosPorHueco = Validaciones.rango(d.minutosPorHueco(), MINUTOS_HUECO_MIN, MINUTOS_HUECO_MAX,
                "minutos por hueco");
        long minutos = Duration.between(inicio, fin).toMinutes();
        if (minutos < minutosPorHueco) {
            throw new IllegalArgumentException("La agenda debe durar al menos un hueco (" + minutosPorHueco + " min)");
        }
        for (LocalTime h = inicio; !h.plusMinutes(minutosPorHueco).isAfter(fin); h = h.plusMinutes(minutosPorHueco)) {
            huecos.add(new Hueco(h, h.plusMinutes(minutosPorHueco)));
        }
    }

    /** Indica si esta agenda se solapa en el tiempo con el intervalo [desde, hasta) del mismo día. */
    public boolean seSolapa(LocalDate dia, LocalTime desde, LocalTime hasta) {
        return fecha.equals(dia) && inicio.isBefore(hasta) && desde.isBefore(fin);
    }

    public Hueco hueco(LocalTime hora) {
        return huecos.stream().filter(h -> h.getInicio().equals(hora)).findFirst()
                .orElseThrow(() -> new NoEncontradoException("La agenda " + id + " no tiene hueco a las " + hora));
    }

    public void ocupar(LocalTime hora, long idCita) {
        hueco(hora).ocupar(idCita);
    }

    public void liberar(LocalTime hora) {
        hueco(hora).liberar();
    }

    public List<HuecoLibre> huecosLibres() {
        return huecos.stream().filter(Hueco::libre)
                .map(h -> new HuecoLibre(id, fecha, h.getInicio(), h.getFin(), sala, modalidad, loginMedico)).toList();
    }

    public long huecosOcupados() {
        return huecos.stream().filter(h -> !h.libre()).count();
    }

    public long getId() {
        return id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public String getSala() {
        return sala;
    }

    public Modalidad getModalidad() {
        return modalidad;
    }

    public String getLoginMedico() {
        return loginMedico;
    }

    public LocalTime getInicio() {
        return inicio;
    }

    public LocalTime getFin() {
        return fin;
    }

    public int getMinutosPorHueco() {
        return minutosPorHueco;
    }

    public List<Hueco> getHuecos() {
        return Collections.unmodifiableList(huecos);
    }

    @Override
    public String toString() {
        return "Agenda " + id + " · " + fecha + " " + inicio + "-" + fin + " · sala " + sala + " · " + modalidad
                + " · " + loginMedico + " · " + huecosOcupados() + "/" + huecos.size() + " ocupados";
    }
}
