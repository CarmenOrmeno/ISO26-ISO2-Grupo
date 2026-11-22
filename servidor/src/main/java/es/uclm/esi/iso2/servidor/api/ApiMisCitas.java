package es.uclm.esi.iso2.servidor.api;

import java.util.List;

import es.uclm.esi.iso2.servidor.citas.Cita;

/** CU07 · Gestionar mis citas (paciente; el médico también, si tiene paciente asociado). */
public interface ApiMisCitas {

    List<Cita> misCitas(String token);

    Cita confirmar(String token, long idCita);

    Cita solicitarCambio(String token, long idCita, String motivo);

    void cancelar(String token, long idCita, String motivo);
}
