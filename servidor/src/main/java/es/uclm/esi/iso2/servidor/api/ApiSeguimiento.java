package es.uclm.esi.iso2.servidor.api;

import java.util.List;

import es.uclm.esi.iso2.servidor.seguimiento.DatosSeguimiento;
import es.uclm.esi.iso2.servidor.seguimiento.Seguimiento;

/** CU10 · Prescribir pruebas de seguimiento periódicas (médico) y generar sus revisiones (Temporizador). */
public interface ApiSeguimiento {

    /** El médico es el usuario de la sesión. */
    Seguimiento prescribir(String token, DatosSeguimiento datos);

    void cancelar(String token, long idSeguimiento, String motivo);

    /** Seguimientos que ha prescrito el médico de la sesión. */
    List<Seguimiento> misPrescripciones(String token);

    /** Ejecuta la tarea programada que genera las solicitudes de las revisiones vencidas. */
    int generarRevisiones(String token);
}
