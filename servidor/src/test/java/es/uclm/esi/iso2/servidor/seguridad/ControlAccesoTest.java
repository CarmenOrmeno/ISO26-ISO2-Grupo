package es.uclm.esi.iso2.servidor.seguridad;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private final List<String> auditados = new ArrayList<>();
    private String tokenPaciente;

    @BeforeEach
    void preparar() {
        SsoSimulado sso = new SsoSimulado().registrar("p", "c", "Paciente");
        Repositorio<Usuario, String> usuarios = new RepositorioEnMemoria<>(Usuario::login);
        usuarios.guardar(new Usuario("p", "Paciente", Rol.PACIENTE, "PAC-1"));
        ServicioSesion sesiones = new ServicioSesionImpl(sso, usuarios,
                new RelojAjustable(LocalDateTime.of(2026, 11, 2, 9, 0)));
        politica = new PoliticaPermisos();
        Auditor auditor = (usuario, permiso, recurso, permitido) -> auditados
                .add(usuario.login() + " " + permiso + " " + recurso + " " + (permitido ? "PERMITIDO" : "DENEGADO"));
        acceso = new ControlAcceso(sesiones, politica, auditor);
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

    @Test
    @DisplayName("CU18: se audita el acceso permitido a datos clínicos, con su recurso")
    void auditaDatosClinicos() {
        politica.conceder(Rol.PACIENTE, Permiso.CONSULTAR_MI_HISTORIAL);
        acceso.exigir(tokenPaciente, Permiso.CONSULTAR_MI_HISTORIAL, "historial propio");
        assertEquals(List.of("p CONSULTAR_MI_HISTORIAL historial propio PERMITIDO"), auditados);
    }

    @Test
    @DisplayName("CU18: un acceso permitido sin datos clínicos no se audita")
    void noAuditaSinDatosClinicos() {
        politica.conceder(Rol.PACIENTE, Permiso.CONSULTAR_PERFIL);
        acceso.exigir(tokenPaciente, Permiso.CONSULTAR_PERFIL);
        assertTrue(auditados.isEmpty());
    }

    @Test
    @DisplayName("CU18: todo acceso denegado por falta de permiso se audita (aunque no sea clínico)")
    void auditaDenegados() {
        assertThrows(AccesoDenegadoException.class, () -> acceso.exigir(tokenPaciente, Permiso.CONFIGURAR_SISTEMA));
        assertEquals(List.of("p CONFIGURAR_SISTEMA  DENEGADO"), auditados);
    }

    @Test
    @DisplayName("CU18: una sesión no válida no se audita (no hay usuario al que atribuirla)")
    void sesionInvalidaNoSeAudita() {
        assertThrows(AccesoDenegadoException.class, () -> acceso.exigir("x", Permiso.CONSULTAR_MI_HISTORIAL, null));
        assertTrue(auditados.isEmpty());
    }
}
