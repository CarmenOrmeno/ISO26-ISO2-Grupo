package es.uclm.esi.iso2.servidor.interoperabilidad;

import java.io.Serializable;
import java.time.LocalDateTime;

import es.uclm.esi.iso2.servidor.comun.Validaciones;

/**
 * Resultado de un intercambio con un sistema externo (CU19). Se guarda siempre, también cuando falla.
 *
 * @param procesados elementos importados o enviados
 * @param omitidos   elementos que no hacía falta intercambiar (ya existían o ya se habían enviado)
 * @param rechazados elementos con datos no válidos
 * @param correcta   el sistema respondió y se intercambió todo lo pendiente
 */
public record Sincronizacion(long id, TipoSincronizacion tipo, String sistema, LocalDateTime fecha, int procesados,
        int omitidos, int rechazados, boolean correcta, String detalle) implements Serializable {

    public Sincronizacion {
        Validaciones.obligatorio(tipo, "tipo");
        sistema = Validaciones.texto(sistema, "sistema");
        Validaciones.obligatorio(fecha, "fecha");
        detalle = detalle == null ? "" : detalle;
    }

    @Override
    public String toString() {
        return fecha + " · " + tipo + " · " + sistema + " · " + (correcta ? "correcta" : "con errores") + " · "
                + procesados + " procesados, " + omitidos + " omitidos, " + rechazados + " rechazados"
                + (detalle.isEmpty() ? "" : "\n  " + detalle);
    }
}
