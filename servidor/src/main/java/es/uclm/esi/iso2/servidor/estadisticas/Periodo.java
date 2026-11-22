package es.uclm.esi.iso2.servidor.estadisticas;

import java.time.LocalDate;
import java.time.LocalDateTime;

import es.uclm.esi.iso2.servidor.comun.Validaciones;

/** Intervalo de fechas de una estadística o informe, con ambos extremos incluidos (CU20–CU22). */
public record Periodo(LocalDate desde, LocalDate hasta) {

    public Periodo {
        Validaciones.obligatorio(desde, "desde");
        Validaciones.obligatorio(hasta, "hasta");
        if (hasta.isBefore(desde)) {
            throw new IllegalArgumentException("La fecha final no puede ser anterior a la inicial");
        }
    }

    public boolean contiene(LocalDate dia) {
        return dia != null && !dia.isBefore(desde) && !dia.isAfter(hasta);
    }

    public boolean contiene(LocalDateTime momento) {
        return momento != null && contiene(momento.toLocalDate());
    }

    @Override
    public String toString() {
        return desde + " – " + hasta;
    }
}
