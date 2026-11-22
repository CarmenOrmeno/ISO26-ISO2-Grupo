package es.uclm.esi.iso2.cliente.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.uclm.esi.iso2.servidor.comun.AccesoDenegadoException;
import es.uclm.esi.iso2.servidor.seguridad.Rol;

/** CU01 en el cliente: la sesión y las acciones de cada rol. */
class SesionClienteTest {

    @Test
    @DisplayName("Iniciar sesión da el usuario y sus acciones; cerrar la deja sin sesión")
    void iniciarYCerrar() {
        SesionCliente s = CatalogoAcciones.sesion(Demo.servidor());
        assertFalse(s.iniciada());
        assertEquals(Rol.ADMINISTRATIVO, s.iniciar("admision1", Demo.CLAVE).rol());
        assertTrue(s.iniciada());
        assertEquals("Mi perfil", s.acciones().get(0).nombre());
        s.cerrar();
        assertFalse(s.iniciada());
        assertThrows(IllegalStateException.class, s::acciones);
        s.cerrar(); // cerrar sin sesión no falla
    }

    @Test
    @DisplayName("Credenciales incorrectas: excepción con el mensaje del servidor y sin sesión")
    void credencialesIncorrectas() {
        SesionCliente s = CatalogoAcciones.sesion(Demo.servidor());
        AccesoDenegadoException e = assertThrows(AccesoDenegadoException.class, () -> s.iniciar("medico1", "mala"));
        assertEquals("Usuario o contraseña incorrectos", e.getMessage());
        assertFalse(s.iniciada());
    }

    @Test
    @DisplayName("Iniciar otra sesión cierra la anterior y cambia las acciones")
    void cambiarDeUsuario() {
        SesionCliente s = Demo.sesion("paciente1");
        s.iniciar("admin1", Demo.CLAVE);
        assertEquals(Rol.ADMINISTRADOR, s.usuario().rol());
        assertTrue(s.accionesPorGrupo().containsKey("CU17 Roles y permisos"));
    }

    @Test
    @DisplayName("Grupos de acciones por rol; el médico tiene también los del paciente (D-005)")
    void gruposPorRol() {
        assertEquals(List.of("CU01 Mi cuenta", "CU07 Mis citas", "CU12 Notificaciones", "CU13 Historial",
                "CU15 Incidencias y encuestas", "CU16 Mis seguimientos"),
                List.copyOf(Demo.sesion("paciente1").accionesPorGrupo().keySet()));
        List<String> medico = List.copyOf(Demo.sesion("medico1").accionesPorGrupo().keySet());
        assertTrue(medico.containsAll(List.of("CU04 Solicitar pruebas", "CU08 Realización", "CU09 Informes e imágenes",
                "CU10 Seguimientos", "CU14 Estado y agendas", "CU20 Estadísticas", "CU13 Historial")));
        assertTrue(Demo.sesion("admision1").accionesPorGrupo().keySet().containsAll(List.of("CU02 Pacientes",
                "CU05 Agendas", "CU06 Citas", "CU11 Listas de espera", "CU15 Atención al paciente",
                "CU21 Informes operativos")));
        assertTrue(Demo.sesion("admin1").accionesPorGrupo().keySet().containsAll(List.of("CU03 Catálogo y parámetros",
                "Tareas programadas", "CU17 Roles y permisos", "CU18 Auditoría", "CU19 Integración",
                "CU22 Estadísticas globales")));
    }

    @Test
    @DisplayName("Sin sesión no se puede ejecutar nada")
    void sinSesion() {
        SesionCliente s = CatalogoAcciones.sesion(Demo.servidor());
        Accion perfil = Accion.de("G", "P", (t, v) -> t);
        assertThrows(IllegalStateException.class, () -> s.ejecutar(perfil, List.of()));
    }
}
