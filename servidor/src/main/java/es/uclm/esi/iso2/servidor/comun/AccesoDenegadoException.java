package es.uclm.esi.iso2.servidor.comun;

/** La sesión no es válida o falta el permiso necesario. */
public class AccesoDenegadoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
