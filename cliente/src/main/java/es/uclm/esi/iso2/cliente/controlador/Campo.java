package es.uclm.esi.iso2.cliente.controlador;

import java.util.Arrays;
import java.util.List;

/**
 * Dato que pide una acción al usuario.
 *
 * @param opciones valores posibles (solo para {@link TipoCampo#OPCION} y {@link TipoCampo#SI_NO})
 */
public record Campo(String etiqueta, TipoCampo tipo, List<String> opciones) {

    public Campo {
        opciones = List.copyOf(opciones);
    }

    public static Campo texto(String etiqueta) {
        return new Campo(etiqueta, TipoCampo.TEXTO, List.of());
    }

    public static Campo textoOpcional(String etiqueta) {
        return new Campo(etiqueta, TipoCampo.TEXTO_OPCIONAL, List.of());
    }

    public static Campo fecha(String etiqueta) {
        return new Campo(etiqueta, TipoCampo.FECHA, List.of());
    }

    public static Campo hora(String etiqueta) {
        return new Campo(etiqueta, TipoCampo.HORA, List.of());
    }

    public static Campo entero(String etiqueta) {
        return new Campo(etiqueta, TipoCampo.ENTERO, List.of());
    }

    public static Campo id(String etiqueta) {
        return new Campo(etiqueta, TipoCampo.LARGO, List.of());
    }

    public static Campo idOpcional(String etiqueta) {
        return new Campo(etiqueta, TipoCampo.LARGO_OPCIONAL, List.of());
    }

    public static Campo siNo(String etiqueta) {
        return new Campo(etiqueta, TipoCampo.SI_NO, List.of("SI", "NO"));
    }

    /** Uno de los valores de un enumerado del servidor (modalidad, prioridad, rol…). */
    public static Campo opcion(String etiqueta, Enum<?>[] valores) {
        return new Campo(etiqueta, TipoCampo.OPCION, Arrays.stream(valores).map(Enum::name).toList());
    }

    /** Etiqueta con la pista de formato, p. ej. «Fecha (AAAA-MM-DD)». */
    public String etiquetaConAyuda() {
        String ayuda = opciones.isEmpty() ? tipo.ayuda() : String.join(", ", opciones);
        return etiqueta + " (" + ayuda + ")";
    }
}
