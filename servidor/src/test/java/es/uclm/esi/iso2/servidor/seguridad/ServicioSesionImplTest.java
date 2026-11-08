package es.uclm.esi.iso2.servidor.seguridad;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.uclm.esi.iso2.servidor.comun.AccesoDenegadoException;
import es.uclm.esi.iso2.servidor.comun.RelojAjustable;
import es.uclm.esi.iso2.servidor.comun.Repositorio;
import es.uclm.esi.iso2.servidor.comun.RepositorioEnMemoria;
import es.uclm.esi.iso2.servidor.integracion.SsoSimulado;

class ServicioSesionImplTest {

    private RelojAjustable reloj;
    private Repositorio<Usuario, String> usuarios;
    private ServicioSesion servicio;

    @BeforeEach
    void preparar() {
        reloj = new RelojAjustable(LocalDateTime.of(2026, 11, 2, 9, 0));
        SsoSimulado sso = new SsoSimulado()
                .registrar("medico1", "clave", "Dr. Ruiz")
                .registrar("externo", "clave", "Sin alta en la aplicación");
        usuarios = new RepositorioEnMemoria<>(Usuario::login);
        usuarios.guardar(new Usuario("medico1", "Dr. Ruiz", Rol.MEDICO, null));
        servicio = new ServicioSesionImpl(sso, usuarios, reloj);
    }

    @Test
    @DisplayName("Credenciales válidas abren una sesión con token único")
    void iniciarSesion() {
        Sesion s1 = servicio.iniciarSesion("medico1", "clave");
        Sesion s2 = servicio.iniciarSesion("medico1", "clave");
        assertNotEquals(s1.getToken(), s2.getToken());
        assertEquals("medico1", servicio.usuarioDe(s1.getToken()).login());
        assertEquals(2, servicio.sesionesIniciadas());
    }

    @Test
    @DisplayName("Contraseña incorrecta: acceso denegado y no cuenta como sesión")
    void contrasenaIncorrecta() {
        assertThrows(AccesoDenegadoException.class, () -> servicio.iniciarSesion("medico1", "mala"));
        assertEquals(0, servicio.sesionesIniciadas());
    }

    @Test
    @DisplayName("Autenticado en el SSO pero sin alta en la aplicación: acceso denegado")
    void sinAltaEnLaAplicacion() {
        assertThrows(AccesoDenegadoException.class, () -> servicio.iniciarSesion("externo", "clave"));
    }

    @Test
    @DisplayName("Tras cerrar sesión el token deja de ser válido")
    void cerrarSesion() {
        String token = servicio.iniciarSesion("medico1", "clave").getToken();
        servicio.cerrarSesion(token);
        assertThrows(AccesoDenegadoException.class, () -> servicio.usuarioDe(token));
    }

    @Test
    @DisplayName("Token nulo o inventado: acceso denegado")
    void tokenInvalido() {
        assertThrows(AccesoDenegadoException.class, () -> servicio.usuarioDe(null));
        assertThrows(AccesoDenegadoException.class, () -> servicio.usuarioDe("inventado"));
    }

    @Test
    @DisplayName("La sesión caduca tras 30 min de inactividad y se renueva con cada uso")
    void caducidad() {
        String token = servicio.iniciarSesion("medico1", "clave").getToken();
        reloj.avanzar(Duration.ofMinutes(29));
        servicio.usuarioDe(token);
        reloj.avanzar(Duration.ofMinutes(29));
        assertEquals("medico1", servicio.usuarioDe(token).login());
        reloj.avanzar(Duration.ofMinutes(30));
        assertThrows(AccesoDenegadoException.class, () -> servicio.usuarioDe(token));
        assertThrows(AccesoDenegadoException.class, () -> servicio.usuarioDe(token));
    }

    @Test
    @DisplayName("Si el usuario se elimina de la aplicación su sesión deja de valer")
    void usuarioEliminado() {
        String token = servicio.iniciarSesion("medico1", "clave").getToken();
        usuarios.eliminar("medico1");
        assertThrows(AccesoDenegadoException.class, () -> servicio.usuarioDe(token));
    }
}
