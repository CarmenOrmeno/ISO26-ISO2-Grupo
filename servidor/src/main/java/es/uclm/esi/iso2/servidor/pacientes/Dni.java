package es.uclm.esi.iso2.servidor.pacientes;

import java.util.Locale;
import java.util.regex.Pattern;

/** Validación del DNI español: 8 dígitos y la letra de control correcta. */
public final class Dni {

    private static final String LETRAS = "TRWAGMYFPDXBNJZSQVHLCKE";
    private static final Pattern FORMATO = Pattern.compile("\\d{8}[A-Z]");

    private Dni() {
    }

    /** Devuelve el DNI normalizado (mayúsculas, sin espacios) o lanza IllegalArgumentException. */
    public static String normalizar(String dni) {
        if (dni == null) {
            throw new IllegalArgumentException("El DNI es obligatorio");
        }
        String valor = dni.strip().toUpperCase(Locale.ROOT);
        if (!FORMATO.matcher(valor).matches()) {
            throw new IllegalArgumentException("El DNI debe tener 8 dígitos y una letra: " + dni);
        }
        int numero = Integer.parseInt(valor.substring(0, 8));
        if (LETRAS.charAt(numero % 23) != valor.charAt(8)) {
            throw new IllegalArgumentException("La letra del DNI no es correcta: " + dni);
        }
        return valor;
    }
}
