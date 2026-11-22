package es.uclm.esi.iso2.servidor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.uclm.esi.iso2.servidor.catalogo.Prioridad;
import es.uclm.esi.iso2.servidor.comun.RelojAjustable;
import es.uclm.esi.iso2.servidor.persistencia.BdPruebas;
import es.uclm.esi.iso2.servidor.persistencia.FabricaMySql;
import es.uclm.esi.iso2.servidor.seguridad.Permiso;
import es.uclm.esi.iso2.servidor.seguridad.Rol;
import es.uclm.esi.iso2.servidor.solicitudes.DatosSolicitud;

/** D-018 · Integración: el servidor completo conserva sus datos en MySQL entre arranques. */
class ServidorMySqlTest {

    private final RelojAjustable reloj = new RelojAjustable(LocalDateTime.of(2026, 11, 2, 9, 0));
    private FabricaMySql fabrica;

    @BeforeEach
    void preparar() {
        fabrica = new FabricaMySql(BdPruebas.vacia());
    }

    private Servidor arrancar() {
        return Servidor.conDatosDemo(reloj, fabrica);
    }

    private static String token(Servidor s, String login) {
        return s.sesion().iniciarSesion(login, DatosDemo.CONTRASENA);
    }

    @Test
    @DisplayName("Al reiniciar se conservan los datos y no se duplican los de demostración")
    void reinicio() {
        Servidor primero = arrancar();
        assertTrue(primero.almacenamiento().startsWith("MySQL"));
        assertEquals(2, primero.estadoSolicitudes().misSolicitudes(token(primero, "medico1")).size());

        Servidor segundo = arrancar();
        assertEquals(2, segundo.estadoSolicitudes().misSolicitudes(token(segundo, "medico1")).size());
        assertEquals(1, segundo.misSeguimientos().misSeguimientos(token(segundo, "paciente1")).size());
    }

    @Test
    @DisplayName("Los ids continúan tras el reinicio (no se reutilizan)")
    void idsContinuan() {
        arrancar();
        Servidor segundo = arrancar();
        long id = segundo.solicitudes().solicitar(token(segundo, "medico1"), new DatosSolicitud("PAC-2",
                "RX-TORAX", "Disnea", "Descartar derrame", Prioridad.PREFERENTE)).getId();
        assertEquals(3, id);
    }

    @Test
    @DisplayName("Los parámetros del sistema (CU03) se conservan tras el reinicio")
    void parametros() {
        Servidor primero = arrancar();
        primero.catalogo().cambiarHorasRecordatorio(token(primero, "admin1"), 48);
        Servidor segundo = arrancar();
        assertTrue(segundo.catalogo().parametros(token(segundo, "admin1")).contains("48 h"));
    }

    @Test
    @DisplayName("Una cita ya recordada no se vuelve a recordar después de reiniciar (CU12)")
    void recordatorios() {
        Servidor primero = arrancar();
        assertEquals(1, primero.notificaciones().enviarRecordatorios(token(primero, "admin1")));
        Servidor segundo = arrancar();
        assertEquals(0, segundo.notificaciones().enviarRecordatorios(token(segundo, "admin1")));
    }

    @Test
    @DisplayName("Los cambios de permisos y de rol (CU17) se conservan tras el reinicio")
    void permisos() {
        Servidor primero = arrancar();
        primero.permisos().revocar(token(primero, "admin1"), Rol.PACIENTE, Permiso.RESPONDER_ENCUESTAS);
        primero.permisos().cambiarRol(token(primero, "admin1"), "admision1", Rol.MEDICO);
        Servidor segundo = arrancar();
        String admin = token(segundo, "admin1");
        assertFalse(segundo.permisos().permisosDe(admin, Rol.PACIENTE).contains(Permiso.RESPONDER_ENCUESTAS));
        assertTrue(segundo.permisos().permisosDe(admin, Rol.PACIENTE).contains(Permiso.COMUNICAR_INCIDENCIAS));
        assertEquals(Rol.MEDICO, segundo.permisos().usuarios(admin).stream()
                .filter(u -> u.login().equals("admision1")).findFirst().orElseThrow().rol());
    }
}
