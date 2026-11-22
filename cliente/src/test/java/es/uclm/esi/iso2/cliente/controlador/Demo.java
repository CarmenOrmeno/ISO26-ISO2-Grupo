package es.uclm.esi.iso2.cliente.controlador;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

import es.uclm.esi.iso2.servidor.Servidor;

/** Servidor de demostración en memoria con el reloj parado el lunes 02/11/2026 a las 9:00. */
final class Demo {

    static final String CLAVE = "demo";

    private Demo() {
    }

    static Servidor servidor() {
        ZoneId zona = ZoneId.systemDefault();
        return Servidor.conDatosDemo(Clock.fixed(LocalDateTime.of(2026, 11, 2, 9, 0).atZone(zona).toInstant(), zona));
    }

    static SesionCliente sesion(String login) {
        SesionCliente s = CatalogoAcciones.sesion(servidor());
        s.iniciar(login, CLAVE);
        return s;
    }

    static Accion accion(SesionCliente s, String nombre) {
        return s.acciones().stream().filter(a -> a.nombre().equals(nombre)).findFirst().orElseThrow();
    }
}
