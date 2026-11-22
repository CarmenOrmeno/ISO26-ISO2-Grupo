package es.uclm.esi.iso2.servidor.interoperabilidad;

/** Situación de un sistema externo en el momento de la consulta (CU19). */
public record EstadoSistema(String nombre, boolean disponible) {

    @Override
    public String toString() {
        return nombre + ": " + (disponible ? "disponible" : "NO disponible");
    }
}
