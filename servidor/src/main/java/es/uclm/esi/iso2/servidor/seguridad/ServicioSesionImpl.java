package es.uclm.esi.iso2.servidor.seguridad;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import es.uclm.esi.iso2.servidor.comun.AccesoDenegadoException;
import es.uclm.esi.iso2.servidor.comun.Repositorio;
import es.uclm.esi.iso2.servidor.integracion.IdentidadSso;
import es.uclm.esi.iso2.servidor.integracion.ProveedorSso;

/** Sesiones en memoria autenticadas con el SSO corporativo. */
public class ServicioSesionImpl implements ServicioSesion {

    /** Tiempo de inactividad tras el que caduca una sesión. */
    public static final Duration INACTIVIDAD_MAXIMA = Duration.ofMinutes(30);

    private final ProveedorSso sso;
    private final Repositorio<Usuario, String> usuarios;
    private final Clock reloj;
    private final Map<String, Sesion> sesiones = new HashMap<>();
    private long sesionesIniciadas;

    public ServicioSesionImpl(ProveedorSso sso, Repositorio<Usuario, String> usuarios, Clock reloj) {
        this.sso = sso;
        this.usuarios = usuarios;
        this.reloj = reloj;
    }

    @Override
    public Sesion iniciarSesion(String login, String contrasena) {
        IdentidadSso identidad = sso.autenticar(login, contrasena)
                .orElseThrow(() -> new AccesoDenegadoException("Usuario o contraseña incorrectos"));
        if (!usuarios.existe(identidad.login())) {
            throw new AccesoDenegadoException("El usuario " + identidad.login() + " no tiene acceso a la aplicación");
        }
        Sesion sesion = new Sesion(UUID.randomUUID().toString(), identidad.login(), LocalDateTime.now(reloj));
        sesiones.put(sesion.getToken(), sesion);
        sesionesIniciadas++;
        return sesion;
    }

    @Override
    public void cerrarSesion(String token) {
        sesiones.remove(token);
    }

    @Override
    public Usuario usuarioDe(String token) {
        Sesion sesion = token == null ? null : sesiones.get(token);
        if (sesion == null) {
            throw new AccesoDenegadoException("Sesión no válida");
        }
        LocalDateTime ahora = LocalDateTime.now(reloj);
        if (sesion.caducada(ahora, INACTIVIDAD_MAXIMA)) {
            sesiones.remove(token);
            throw new AccesoDenegadoException("La sesión ha caducado");
        }
        sesion.registrarAcceso(ahora);
        return usuarios.buscar(sesion.getLogin())
                .orElseThrow(() -> new AccesoDenegadoException("El usuario ya no tiene acceso"));
    }

    @Override
    public long sesionesIniciadas() {
        return sesionesIniciadas;
    }
}
