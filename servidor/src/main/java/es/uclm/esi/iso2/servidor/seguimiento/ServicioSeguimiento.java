package es.uclm.esi.iso2.servidor.seguimiento;

import java.util.List;

/** Contrato interno de los seguimientos periódicos (CU10, CU16). */
public interface ServicioSeguimiento {

    /** CU10: el médico prescribe un seguimiento de un tipo que lo admite para un paciente activo. */
    Seguimiento prescribir(String loginMedico, DatosSeguimiento datos);

    /** Solo el médico que lo prescribió puede cancelarlo, y solo si está ACTIVO. */
    void cancelar(long idSeguimiento, String loginMedico, String motivo);

    Seguimiento obtener(long idSeguimiento);

    List<Seguimiento> porMedico(String loginMedico);

    List<Seguimiento> porPaciente(String idPaciente);

    /**
     * Tarea programada (actor Temporizador): genera la solicitud de cada revisión vencida. Si el paciente
     * ya tiene una solicitud abierta del mismo tipo, la revisión se aplaza a la siguiente ejecución.
     *
     * @return número de solicitudes generadas
     */
    int generarRevisionesVencidas();
}
