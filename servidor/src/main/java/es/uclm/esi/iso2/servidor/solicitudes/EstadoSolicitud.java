package es.uclm.esi.iso2.servidor.solicitudes;

/** Ciclo de vida de una solicitud (ver docs/wiki/Arquitectura.md §4). */
public enum EstadoSolicitud {
    SOLICITADA,
    CITADA,
    REALIZADA,
    INFORMADA,
    CANCELADA;

    /** Una solicitud está abierta mientras no se ha realizado ni cancelado. */
    public boolean abierta() {
        return this == SOLICITADA || this == CITADA;
    }
}
