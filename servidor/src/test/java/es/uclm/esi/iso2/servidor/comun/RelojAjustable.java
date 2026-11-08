package es.uclm.esi.iso2.servidor.comun;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

/** Reloj de pruebas que se puede adelantar (D-010). */
public class RelojAjustable extends Clock {

    private Instant instante;

    public RelojAjustable(LocalDateTime inicio) {
        this.instante = inicio.toInstant(ZoneOffset.UTC);
    }

    public void avanzar(Duration duracion) {
        instante = instante.plus(duracion);
    }

    public void fijar(LocalDateTime momento) {
        instante = momento.toInstant(ZoneOffset.UTC);
    }

    @Override
    public ZoneId getZone() {
        return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return this;
    }

    @Override
    public Instant instant() {
        return instante;
    }
}
