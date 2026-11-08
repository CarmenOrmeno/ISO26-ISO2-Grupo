package es.uclm.esi.iso2.servidor.agendas;

import java.time.LocalTime;

import es.uclm.esi.iso2.servidor.comun.ReglaNegocioException;

/** Hueco de una agenda. Está libre si no tiene cita asociada. */
public class Hueco {

    private final LocalTime inicio;
    private final LocalTime fin;
    private Long idCita;

    public Hueco(LocalTime inicio, LocalTime fin) {
        this.inicio = inicio;
        this.fin = fin;
    }

    public boolean libre() {
        return idCita == null;
    }

    void ocupar(long cita) {
        if (!libre()) {
            throw new ReglaNegocioException("El hueco de las " + inicio + " ya está ocupado");
        }
        idCita = cita;
    }

    void liberar() {
        idCita = null;
    }

    public LocalTime getInicio() {
        return inicio;
    }

    public LocalTime getFin() {
        return fin;
    }

    public Long getIdCita() {
        return idCita;
    }

    @Override
    public String toString() {
        return inicio + "-" + fin + (libre() ? " libre" : " cita " + idCita);
    }
}
