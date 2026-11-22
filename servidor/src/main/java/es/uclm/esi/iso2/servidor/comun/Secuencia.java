package es.uclm.esi.iso2.servidor.comun;

import java.util.function.ToLongFunction;

/**
 * Generador de identificadores numéricos de un componente. Continúa desde el mayor identificador ya
 * guardado, de modo que no se repiten ids al reiniciar el servidor con una base de datos persistente.
 * El siguiente id solo se consume al {@link #confirmar() confirmar} (si la entidad no pasa la validación,
 * el número se reutiliza).
 */
public final class Secuencia {

    private long ultimo;

    private Secuencia(long ultimo) {
        this.ultimo = ultimo;
    }

    /** Secuencia que sigue al mayor número de las entidades del repositorio (0 si está vacío). */
    public static <T> Secuencia continuando(Repositorio<T, ?> repositorio, ToLongFunction<T> numero) {
        return new Secuencia(repositorio.todos().stream().mapToLong(numero).max().orElse(0));
    }

    /** Número que recibirá la próxima entidad. */
    public long siguiente() {
        return ultimo + 1;
    }

    /** Da por usado el número {@link #siguiente()}. */
    public void confirmar() {
        ultimo++;
    }
}
