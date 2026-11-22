package es.uclm.esi.iso2.servidor.atencion;

import java.io.Serializable;
import java.time.LocalDateTime;

import es.uclm.esi.iso2.servidor.comun.Validaciones;

/**
 * Encuesta de satisfacción sobre una prueba realizada (CU15). Hay como mucho una por prueba, así que se
 * identifica por la solicitud.
 *
 * @param puntuacion de {@value #PUNTUACION_MINIMA} (muy insatisfecho) a {@value #PUNTUACION_MAXIMA} (muy satisfecho)
 * @param comentario opcional (vacío si no hay), como máximo {@value #LONGITUD_COMENTARIO} caracteres
 */
public record Encuesta(long idSolicitud, String idPaciente, int puntuacion, String comentario, LocalDateTime fecha)
        implements Serializable {

    public static final int PUNTUACION_MINIMA = 1;
    public static final int PUNTUACION_MAXIMA = 5;
    public static final int LONGITUD_COMENTARIO = 500;

    public Encuesta {
        Validaciones.texto(idPaciente, "paciente");
        Validaciones.rango(puntuacion, PUNTUACION_MINIMA, PUNTUACION_MAXIMA, "puntuación");
        comentario = comentario == null ? "" : comentario.strip();
        if (comentario.length() > LONGITUD_COMENTARIO) {
            throw new IllegalArgumentException(
                    "El comentario admite como máximo " + LONGITUD_COMENTARIO + " caracteres");
        }
        Validaciones.obligatorio(fecha, "fecha");
    }

    @Override
    public String toString() {
        return "Encuesta de la solicitud " + idSolicitud + " · " + puntuacion + "/" + PUNTUACION_MAXIMA
                + (comentario.isEmpty() ? "" : " · " + comentario);
    }
}
