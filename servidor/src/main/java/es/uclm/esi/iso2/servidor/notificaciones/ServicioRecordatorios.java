package es.uclm.esi.iso2.servidor.notificaciones;

import java.time.Clock;
import java.time.LocalDateTime;

import es.uclm.esi.iso2.servidor.catalogo.ServicioCatalogo;
import es.uclm.esi.iso2.servidor.catalogo.TipoPrueba;
import es.uclm.esi.iso2.servidor.citas.Cita;
import es.uclm.esi.iso2.servidor.citas.ServicioCitas;
import es.uclm.esi.iso2.servidor.comun.Repositorio;
import es.uclm.esi.iso2.servidor.solicitudes.ServicioSolicitudes;

/**
 * Tarea programada (actor Temporizador, CU12): recuerda las citas activas que empiezan dentro de la
 * antelación configurada y avisa de la preparación previa. Cada cita se recuerda una sola vez (las ya
 * recordadas se guardan, también entre reinicios).
 */
public class ServicioRecordatorios {

    private final ServicioCitas citas;
    private final ServicioSolicitudes solicitudes;
    private final ServicioCatalogo catalogo;
    private final Notificador notificador;
    private final Clock reloj;
    private final Repositorio<Long, Long> recordadas;

    public ServicioRecordatorios(ServicioCitas citas, ServicioSolicitudes solicitudes, ServicioCatalogo catalogo,
            Notificador notificador, Repositorio<Long, Long> recordadas, Clock reloj) {
        this.citas = citas;
        this.solicitudes = solicitudes;
        this.catalogo = catalogo;
        this.notificador = notificador;
        this.recordadas = recordadas;
        this.reloj = reloj;
    }

    /** Envía los recordatorios pendientes y devuelve cuántas citas se han recordado. */
    public int enviarRecordatorios() {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        LocalDateTime limite = ahora.plusHours(catalogo.parametros().horasAntelacionRecordatorio());
        int enviados = 0;
        for (Cita cita : citas.todas()) {
            boolean enVentana = cita.getFechaHora().isAfter(ahora) && !cita.getFechaHora().isAfter(limite);
            if (cita.getEstado().activa() && enVentana && !recordadas.existe(cita.getId())) {
                recordadas.guardar(cita.getId());
                TipoPrueba tipo = catalogo.obtener(solicitudes.obtener(cita.getIdSolicitud()).getCodigoTipo());
                notificador.notificar(cita.getIdPaciente(), TipoNotificacion.RECORDATORIO,
                        "Recuerde su cita de " + tipo.nombre() + " el " + cita.getFechaHora());
                if (tipo.necesitaPreparacion()) {
                    notificador.notificar(cita.getIdPaciente(), TipoNotificacion.PREPARACION,
                            "Preparación para " + tipo.nombre() + ": " + tipo.preparacion());
                }
                enviados++;
            }
        }
        return enviados;
    }
}
