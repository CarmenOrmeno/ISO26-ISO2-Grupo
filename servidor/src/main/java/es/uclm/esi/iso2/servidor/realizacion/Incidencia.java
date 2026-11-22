package es.uclm.esi.iso2.servidor.realizacion;

import java.time.LocalDateTime;

import es.uclm.esi.iso2.servidor.comun.Validaciones;

/** Incidencia registrada por el médico durante la realización de una prueba (CU08). */
public record Incidencia(long id, long idCita, TipoIncidencia tipo, String descripcion, String autor,
        LocalDateTime fecha) {

    public Incidencia {
        Validaciones.obligatorio(tipo, "tipo");
        descripcion = Validaciones.texto(descripcion, "descripción");
        autor = Validaciones.texto(autor, "autor");
        Validaciones.obligatorio(fecha, "fecha");
    }

    @Override
    public String toString() {
        return "Incidencia " + id + " · cita " + idCita + " · " + tipo + " · " + descripcion + " (" + autor + ")";
    }
}
