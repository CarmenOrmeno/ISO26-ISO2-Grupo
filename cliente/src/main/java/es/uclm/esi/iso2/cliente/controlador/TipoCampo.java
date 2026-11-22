package es.uclm.esi.iso2.cliente.controlador;

/** Tipo de dato que pide un campo de una acción; decide cómo se interpreta el texto y qué control usa la vista. */
public enum TipoCampo {
    TEXTO("texto"),
    TEXTO_OPCIONAL("opcional"),
    FECHA("AAAA-MM-DD"),
    HORA("HH:MM"),
    ENTERO("número"),
    LARGO("número"),
    LARGO_OPCIONAL("número, opcional"),
    OPCION("elegir"),
    SI_NO("s/n");

    private final String ayuda;

    TipoCampo(String ayuda) {
        this.ayuda = ayuda;
    }

    /** Pista de formato para el usuario. */
    public String ayuda() {
        return ayuda;
    }
}
