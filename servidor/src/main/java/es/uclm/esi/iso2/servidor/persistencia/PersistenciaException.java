package es.uclm.esi.iso2.servidor.persistencia;

/** Fallo técnico del almacenamiento (conexión, SQL, datos corruptos). No es un error de negocio. */
public class PersistenciaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public PersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
