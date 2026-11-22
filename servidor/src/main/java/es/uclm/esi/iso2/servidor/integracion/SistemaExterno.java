package es.uclm.esi.iso2.servidor.integracion;

/** Lo común a los sistemas hospitalarios con los que se integra la aplicación (D-009, CU19). */
public interface SistemaExterno {

    /** Nombre para mostrar al administrador. */
    String nombre();

    /** Si el sistema responde en este momento. */
    boolean disponible();
}
