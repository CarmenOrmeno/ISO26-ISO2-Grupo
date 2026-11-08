package es.uclm.esi.iso2.servidor.informes;

import java.time.LocalDateTime;

import es.uclm.esi.iso2.servidor.comun.Validaciones;

/** Informe radiológico de una prueba realizada (CU09). */
public record Informe(long id, long idSolicitud, String idPaciente, String loginMedico, String hallazgos,
        String conclusion, LocalDateTime fecha) {

    public Informe {
        Validaciones.texto(idPaciente, "paciente");
        Validaciones.texto(loginMedico, "médico");
        hallazgos = Validaciones.texto(hallazgos, "hallazgos");
        conclusion = Validaciones.texto(conclusion, "conclusión");
        Validaciones.obligatorio(fecha, "fecha");
    }

    @Override
    public String toString() {
        return "Informe " + id + " · solicitud " + idSolicitud + " · " + fecha.toLocalDate() + " · " + loginMedico
                + "\n  Hallazgos: " + hallazgos + "\n  Conclusión: " + conclusion;
    }
}
