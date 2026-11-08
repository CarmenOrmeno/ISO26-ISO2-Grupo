package es.uclm.esi.iso2.servidor.agendas;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import es.uclm.esi.iso2.servidor.catalogo.Modalidad;

/** Vista de un hueco libre para buscar dónde citar (CU06, CU11, CU14). */
public record HuecoLibre(long idAgenda, LocalDate fecha, LocalTime inicio, LocalTime fin, String sala,
        Modalidad modalidad, String loginMedico) {

    public LocalDateTime momento() {
        return fecha.atTime(inicio);
    }

    @Override
    public String toString() {
        return "agenda " + idAgenda + " · " + fecha + " " + inicio + "-" + fin + " · sala " + sala + " · " + modalidad;
    }
}
