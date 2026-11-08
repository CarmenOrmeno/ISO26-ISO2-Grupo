package es.uclm.esi.iso2.servidor.comun;

/** Se incumple una regla del negocio. */
public class ReglaNegocioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
