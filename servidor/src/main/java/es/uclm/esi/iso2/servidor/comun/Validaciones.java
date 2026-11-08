package es.uclm.esi.iso2.servidor.comun;

/** Comprobaciones de invariantes que usan las entidades del dominio. */
public final class Validaciones {

    private Validaciones() {
    }

    /** Exige un texto no nulo ni en blanco y lo devuelve sin espacios a los lados. */
    public static String texto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El campo «" + campo + "» es obligatorio");
        }
        return valor.strip();
    }

    /** Exige un objeto no nulo. */
    public static <T> T obligatorio(T valor, String campo) {
        if (valor == null) {
            throw new IllegalArgumentException("El campo «" + campo + "» es obligatorio");
        }
        return valor;
    }

    /** Exige un entero en el intervalo cerrado [min, max]. */
    public static int rango(int valor, int min, int max, String campo) {
        if (valor < min || valor > max) {
            throw new IllegalArgumentException(
                    "El campo «" + campo + "» debe estar entre " + min + " y " + max + " (es " + valor + ")");
        }
        return valor;
    }
}
