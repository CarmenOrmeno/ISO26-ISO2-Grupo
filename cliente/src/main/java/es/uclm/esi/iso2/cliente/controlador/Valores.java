package es.uclm.esi.iso2.cliente.controlador;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Valores que el usuario ha escrito para los campos de una acción, ya interpretados. Se comprueban todos al
 * crearlos: si alguno no tiene el formato de su tipo, lanza {@link IllegalArgumentException} con el nombre del
 * campo y no se llama al servidor.
 */
public final class Valores {

    private final List<Object> valores;

    private Valores(List<Object> valores) {
        this.valores = valores;
    }

    public static Valores de(List<Campo> campos, List<String> textos) {
        if (campos.size() != textos.size()) {
            throw new IllegalArgumentException("Se esperaban " + campos.size() + " valores y hay " + textos.size());
        }
        List<Object> interpretados = new ArrayList<>();
        for (int i = 0; i < campos.size(); i++) {
            interpretados.add(interpretar(campos.get(i), textos.get(i) == null ? "" : textos.get(i).strip()));
        }
        return new Valores(interpretados);
    }

    private static Object interpretar(Campo campo, String texto) {
        boolean opcional = campo.tipo() == TipoCampo.TEXTO_OPCIONAL || campo.tipo() == TipoCampo.LARGO_OPCIONAL;
        if (texto.isEmpty()) {
            if (opcional) {
                return null;
            }
            throw new IllegalArgumentException("El campo «" + campo.etiqueta() + "» es obligatorio");
        }
        try {
            return switch (campo.tipo()) {
                case TEXTO, TEXTO_OPCIONAL -> texto;
                case FECHA -> LocalDate.parse(texto);
                case HORA -> LocalTime.parse(texto);
                case ENTERO -> Integer.parseInt(texto);
                case LARGO, LARGO_OPCIONAL -> Long.parseLong(texto);
                case OPCION, SI_NO -> opcion(campo, texto);
            };
        } catch (NumberFormatException | DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "«" + texto + "» no es válido en «" + campo.etiqueta() + "» (" + campo.tipo().ayuda() + ")");
        }
    }

    private static String opcion(Campo campo, String texto) {
        String mayusculas = texto.toUpperCase(Locale.ROOT);
        if (campo.tipo() == TipoCampo.SI_NO && mayusculas.length() == 1) {
            mayusculas = mayusculas.equals("S") ? "SI" : mayusculas.equals("N") ? "NO" : mayusculas;
        }
        if (!campo.opciones().contains(mayusculas)) {
            throw new IllegalArgumentException("«" + texto + "» no es válido en «" + campo.etiqueta()
                    + "»; opciones: " + String.join(", ", campo.opciones()));
        }
        return mayusculas;
    }

    /** Texto obligatorio, o {@code null} si era opcional y se dejó vacío. */
    public String texto(int i) {
        return (String) valores.get(i);
    }

    /** Texto opcional: vacío si no se escribió nada. */
    public String textoOVacio(int i) {
        String t = texto(i);
        return t == null ? "" : t;
    }

    public LocalDate fecha(int i) {
        return (LocalDate) valores.get(i);
    }

    public LocalTime hora(int i) {
        return (LocalTime) valores.get(i);
    }

    public int entero(int i) {
        return (Integer) valores.get(i);
    }

    public long largo(int i) {
        return (Long) valores.get(i);
    }

    /** Número opcional: {@code null} si no se escribió nada. */
    public Long largoOpcional(int i) {
        return (Long) valores.get(i);
    }

    public boolean siNo(int i) {
        return "SI".equals(valores.get(i));
    }

    public <E extends Enum<E>> E opcion(int i, Class<E> tipo) {
        return Enum.valueOf(tipo, (String) valores.get(i));
    }
}
