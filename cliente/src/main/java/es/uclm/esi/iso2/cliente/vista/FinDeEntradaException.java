package es.uclm.esi.iso2.cliente.vista;

/** Se ha cerrado la entrada estándar: la aplicación termina ordenadamente. */
public class FinDeEntradaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public FinDeEntradaException() {
        super("Fin de la entrada");
    }
}
