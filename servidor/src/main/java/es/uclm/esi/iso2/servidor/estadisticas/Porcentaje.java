package es.uclm.esi.iso2.servidor.estadisticas;

import java.util.Locale;
import java.util.OptionalDouble;

/** Cálculos comunes de las estadísticas. */
final class Porcentaje {

    private Porcentaje() {
    }

    /** parte / total × 100, o vacío si no hay total (no se puede calcular). */
    static OptionalDouble de(long parte, long total) {
        return total == 0 ? OptionalDouble.empty() : OptionalDouble.of(100.0 * parte / total);
    }

    /** Texto con un decimal y la unidad, o «sin datos». */
    static String texto(OptionalDouble valor, String unidad) {
        return valor.isPresent() ? String.format(Locale.ROOT, "%.1f%s", valor.getAsDouble(), unidad) : "sin datos";
    }
}
