package es.uclm.esi.iso2.servidor.comun;

/** No existe la entidad solicitada. */
public class NoEncontradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public NoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
