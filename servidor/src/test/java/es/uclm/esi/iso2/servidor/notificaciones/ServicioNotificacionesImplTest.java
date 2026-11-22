package es.uclm.esi.iso2.servidor.notificaciones;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.uclm.esi.iso2.servidor.comun.AccesoDenegadoException;
import es.uclm.esi.iso2.servidor.comun.NoEncontradoException;
import es.uclm.esi.iso2.servidor.comun.RelojAjustable;
import es.uclm.esi.iso2.servidor.comun.RepositorioEnMemoria;

class ServicioNotificacionesImplTest {

    private final ServicioNotificaciones servicio = new ServicioNotificacionesImpl(
            new RepositorioEnMemoria<>(Notificacion::getId), new RelojAjustable(LocalDateTime.of(2026, 11, 2, 9, 0)));

    @Test
    @DisplayName("Buzón por paciente, de la más reciente a la más antigua, con contador de no leídas")
    void buzon() {
        servicio.notificar("PAC-1", TipoNotificacion.CITA_ASIGNADA, "uno");
        servicio.notificar("PAC-2", TipoNotificacion.CITA_ASIGNADA, "otro paciente");
        servicio.notificar("PAC-1", TipoNotificacion.RECORDATORIO, "dos");
        assertEquals("dos", servicio.dePaciente("PAC-1").get(0).getMensaje());
        assertEquals(2, servicio.noLeidas("PAC-1"));
        servicio.marcarLeida(1, "PAC-1");
        assertEquals(1, servicio.noLeidas("PAC-1"));
        assertTrue(servicio.dePaciente("PAC-1").get(1).toString().startsWith("  [1]"));
        assertTrue(servicio.dePaciente("PAC-1").get(0).toString().startsWith("* [3]"));
    }

    @Test
    @DisplayName("Marcar como leída una notificación ajena o inexistente")
    void marcarAjena() {
        servicio.notificar("PAC-1", TipoNotificacion.CITA_ASIGNADA, "uno");
        assertThrows(AccesoDenegadoException.class, () -> servicio.marcarLeida(1, "PAC-2"));
        assertThrows(NoEncontradoException.class, () -> servicio.marcarLeida(9, "PAC-1"));
    }

    @Test
    @DisplayName("Notificación con datos vacíos se rechaza")
    void invalida() {
        assertThrows(IllegalArgumentException.class, () -> servicio.notificar("", TipoNotificacion.RECORDATORIO, "m"));
        assertThrows(IllegalArgumentException.class, () -> servicio.notificar("PAC-1", null, "m"));
        assertThrows(IllegalArgumentException.class, () -> servicio.notificar("PAC-1", TipoNotificacion.RECORDATORIO, " "));
        assertThrows(IllegalArgumentException.class,
                () -> new Notificacion(1, "PAC-1", TipoNotificacion.RECORDATORIO, "m", null));
    }
}
