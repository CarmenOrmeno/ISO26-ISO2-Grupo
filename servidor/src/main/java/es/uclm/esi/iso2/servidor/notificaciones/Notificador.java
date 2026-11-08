package es.uclm.esi.iso2.servidor.notificaciones;

/**
 * Canal de avisos al paciente. Los componentes que generan avisos (citas, informes, seguimiento) solo
 * conocen esta interfaz: un canal nuevo (SMS, correo, app) es otra implementación (abierto/cerrado).
 */
public interface Notificador {

    void notificar(String idPaciente, TipoNotificacion tipo, String mensaje);
}
