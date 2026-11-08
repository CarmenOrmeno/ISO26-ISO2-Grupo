package es.uclm.esi.iso2.servidor.seguridad;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.uclm.esi.iso2.servidor.comun.AccesoDenegadoException;
import es.uclm.esi.iso2.servidor.comun.RelojAjustable;
import es.uclm.esi.iso2.servidor.comun.Repositorio;
import es.uclm.esi.iso2.servidor.comun.RepositorioEnMemoria;
import es.uclm.esi.iso2.servidor.integracion.SsoSimulado;

class ControlAccesoTest {

    private PoliticaPermisos politica;
    private ControlAcceso acceso;
    private String tokenPaciente;

    @BeforeEach
    void preparar() {
        SsoSimulado sso = new SsoSimulado().registrar("p", "c", "Paciente");
        Repositorio<Usuario, String> usuarios = new RepositorioEnMemoria<>(Usuario::login);
        usuarios.guardar(new Usuario("p", "Paciente", Rol.PACIENTE, "PAC-1"));
        ServicioSesion sesiones = new ServicioSesionImpl(sso, usuarios,
                new RelojAjustable(LocalDateTime.of(2026, 11, 2, 9, 0)));
        politica = new PoliticaPermisos();
        acceso = new ControlAcceso(sesiones, politica);
        tokenPaciente = sesiones.iniciarSesion("p", "c").getToken();
    }

    @Test
    @DisplayName("Con el permiso concedido devuelve el usuario")
    void conPermiso() {
        politica.conceder(Rol.PACIENTE, Permiso.CONSULTAR_PERFIL);
        assertEquals("p", acceso.exigir(tokenPaciente, Permiso.CONSULTAR_PERFIL).login());
    }

    @Test
    @DisplayName("Sin el permiso lanza AccesoDenegadoException")
    void sinPermiso() {
        assertThrows(AccesoDenegadoException.class, () -> acceso.exigir(tokenPaciente, Permiso.CONSULTAR_PERFIL));
    }

    @Test
    @DisplayName("Con un token inválido lanza AccesoDenegadoException aunque el rol tenga el permiso")
    void tokenInvalido() {
        politica.conceder(Rol.PACIENTE, Permiso.CONSULTAR_PERFIL);
        assertThrows(AccesoDenegadoException.class, () -> acceso.exigir("x", Permiso.CONSULTAR_PERFIL));
    }

    @Test
    @DisplayName("Revocar un permiso lo quita")
    void revocar() {
        politica.conceder(Rol.PACIENTE, Permiso.CONSULTAR_PERFIL);
        politica.revocar(Rol.PACIENTE, Permiso.CONSULTAR_PERFIL);
        assertTrue(politica.permisosDe(Rol.PACIENTE).isEmpty());
    }
}
