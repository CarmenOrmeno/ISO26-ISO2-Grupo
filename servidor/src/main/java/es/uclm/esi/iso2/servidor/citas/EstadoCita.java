package es.uclm.esi.iso2.servidor.citas;

/** Estados de una cita. */
public enum EstadoCita {
    PROGRAMADA,
    CONFIRMADA,
    CAMBIO_SOLICITADO,
    CANCELADA,
    REALIZADA;

    /** Una cita activa ocupa un hueco de agenda. */
    public boolean activa() {
        return this == PROGRAMADA || this == CONFIRMADA || this == CAMBIO_SOLICITADO;
    }
}
