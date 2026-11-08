package es.uclm.esi.iso2.servidor.citas;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** Contrato interno del componente de citas. */
public interface ServicioCitas {

    /** CU06: cita una solicitud pendiente en un hueco libre de la modalidad de su tipo de prueba. */
    Cita asignar(long idSolicitud, long idAgenda, LocalTime hora);

    Cita reprogramar(long idCita, long idAgenda, LocalTime hora);

    /** Cancela la cita, libera el hueco y la solicitud vuelve a la lista de espera. */
    void cancelar(long idCita, String motivo);

    Cita obtener(long idCita);

    List<Cita> dePaciente(String idPaciente);

    List<Cita> delDia(LocalDate fecha);

    List<Cita> todas();

    /** CU07: el paciente confirma su cita. */
    Cita confirmar(long idCita, String idPaciente);

    /** CU07: el paciente solicita un cambio de su cita. */
    Cita solicitarCambio(long idCita, String idPaciente, String motivo);

    /** CU07: el paciente cancela su cita. */
    void cancelarPorPaciente(long idCita, String idPaciente, String motivo);

    /** Citas con cambio solicitado, para que admisión las reprograme. */
    List<Cita> conCambioSolicitado();

    /** CU08: marca la cita como realizada (lo invoca el componente de realización). */
    Cita marcarRealizada(long idCita);
}
