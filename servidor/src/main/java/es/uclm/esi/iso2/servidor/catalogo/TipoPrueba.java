package es.uclm.esi.iso2.servidor.catalogo;

import es.uclm.esi.iso2.servidor.comun.Validaciones;

/**
 * Tipo de prueba del catálogo (CU03).
 *
 * @param duracionMinutos duración estimada, entre {@value #DURACION_MINIMA} y {@value #DURACION_MAXIMA}
 * @param preparacion     preparación previa del paciente ("" si no necesita)
 */
public record TipoPrueba(String codigo, String nombre, Modalidad modalidad, String sala, int duracionMinutos,
        String preparacion, boolean contraste, boolean consentimiento, String requisitosSeguridad,
        boolean admiteSeguimiento) {

    public static final int DURACION_MINIMA = 5;
    public static final int DURACION_MAXIMA = 240;

    public TipoPrueba {
        codigo = Validaciones.texto(codigo, "código");
        nombre = Validaciones.texto(nombre, "nombre");
        Validaciones.obligatorio(modalidad, "modalidad");
        sala = Validaciones.texto(sala, "sala");
        Validaciones.rango(duracionMinutos, DURACION_MINIMA, DURACION_MAXIMA, "duración (min)");
        preparacion = preparacion == null ? "" : preparacion.strip();
        requisitosSeguridad = requisitosSeguridad == null ? "" : requisitosSeguridad.strip();
        if (contraste && !consentimiento) {
            throw new IllegalArgumentException("Una prueba con contraste exige consentimiento informado");
        }
    }

    public boolean necesitaPreparacion() {
        return !preparacion.isEmpty();
    }

    @Override
    public String toString() {
        return codigo + " · " + nombre + " (" + modalidad + ", " + duracionMinutos + " min, sala " + sala + ")"
                + (contraste ? " · contraste" : "") + (admiteSeguimiento ? " · admite seguimiento" : "");
    }
}
