package es.uclm.esi.iso2.servidor.auditoria;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.uclm.esi.iso2.servidor.comun.RelojAjustable;
import es.uclm.esi.iso2.servidor.comun.Repositorio;
import es.uclm.esi.iso2.servidor.persistencia.RepositorioEnMemoria;
import es.uclm.esi.iso2.servidor.seguridad.Permiso;
import es.uclm.esi.iso2.servidor.seguridad.Rol;
import es.uclm.esi.iso2.servidor.seguridad.Usuario;

/** CU18 · Registro y consultas de la auditoría. */
class ServicioAuditoriaImplTest {

    private static final LocalDate LUNES = LocalDate.of(2026, 11, 2);
    private static final Usuario MEDICO = new Usuario("medico1", "Dr. Ruiz", Rol.MEDICO, null);
    private static final Usuario ADMISION = new Usuario("admision1", "Marta", Rol.ADMINISTRATIVO, null);

    private final RelojAjustable reloj = new RelojAjustable(LUNES.atTime(9, 0));
    private final Repositorio<RegistroAuditoria, Long> almacen = new RepositorioEnMemoria<>(RegistroAuditoria::id);
    private ServicioAuditoria auditoria = new ServicioAuditoriaImpl(almacen, reloj);

    /** Lunes: médico lee el informe 7 (permitido). Martes: admisión intenta leerlo (denegado). Miércoles: médico ve imágenes 8. */
    @BeforeEach
    void preparar() {
        auditoria.registrar(MEDICO, Permiso.CONSULTAR_INFORMES_PACIENTES, "informe de la solicitud 7", true);
        reloj.fijar(LUNES.plusDays(1).atTime(10, 0));
        auditoria.registrar(ADMISION, Permiso.CONSULTAR_INFORMES_PACIENTES, "informe de la solicitud 7", false);
        reloj.fijar(LUNES.plusDays(2).atTime(11, 0));
        auditoria.registrar(MEDICO, Permiso.CONSULTAR_INFORMES_PACIENTES, "Imágenes de la solicitud 8", true);
    }

    private static List<Long> ids(List<RegistroAuditoria> registros) {
        return registros.stream().map(RegistroAuditoria::id).toList();
    }

    @Test
    @DisplayName("Cada registro guarda usuario, rol, operación, recurso, fecha y resultado; el más reciente primero")
    void registrar() {
        List<RegistroAuditoria> todos = auditoria.registros(LUNES, LUNES.plusDays(2));
        assertEquals(List.of(3L, 2L, 1L), ids(todos));
        RegistroAuditoria denegado = todos.get(1);
        assertEquals("admision1", denegado.login());
        assertEquals(Rol.ADMINISTRATIVO, denegado.rol());
        assertEquals(Permiso.CONSULTAR_INFORMES_PACIENTES, denegado.operacion());
        assertEquals(LUNES.plusDays(1).atTime(10, 0), denegado.fecha());
        assertFalse(denegado.permitido());
        assertTrue(denegado.toString().endsWith("DENEGADO"));
    }

    @Test
    @DisplayName("Valores límite del intervalo: desde = hasta = un día; días sin registros; hasta < desde")
    void intervalo() {
        assertEquals(List.of(2L), ids(auditoria.registros(LUNES.plusDays(1), LUNES.plusDays(1))));
        assertTrue(auditoria.registros(LUNES.minusDays(7), LUNES.minusDays(1)).isEmpty());
        assertTrue(auditoria.registros(LUNES.plusDays(3), LUNES.plusDays(9)).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> auditoria.registros(LUNES, LUNES.minusDays(1)));
        assertThrows(IllegalArgumentException.class, () -> auditoria.registros(null, LUNES));
    }

    @Test
    @DisplayName("Filtros por usuario, denegados y recurso (sin distinguir mayúsculas)")
    void filtros() {
        LocalDate fin = LUNES.plusDays(2);
        assertEquals(List.of(3L, 1L), ids(auditoria.deUsuario("medico1", LUNES, fin)));
        assertTrue(auditoria.deUsuario("nadie", LUNES, fin).isEmpty());
        assertEquals(List.of(2L), ids(auditoria.denegados(LUNES, fin)));
        assertEquals(List.of(2L, 1L), ids(auditoria.sobreRecurso("SOLICITUD 7", LUNES, fin)));
        assertEquals(List.of(3L), ids(auditoria.sobreRecurso("imágenes", LUNES, fin)));
        assertThrows(IllegalArgumentException.class, () -> auditoria.sobreRecurso(" ", LUNES, fin));
        assertThrows(IllegalArgumentException.class, () -> auditoria.deUsuario(null, LUNES, fin));
    }

    @Test
    @DisplayName("Los ids continúan tras reiniciar con el mismo almacén")
    void idsContinuan() {
        auditoria = new ServicioAuditoriaImpl(almacen, reloj);
        auditoria.registrar(MEDICO, Permiso.CONSULTAR_INFORMES_PACIENTES, null, true);
        RegistroAuditoria ultimo = auditoria.registros(LUNES, LUNES.plusDays(2)).get(0);
        assertEquals(4, ultimo.id());
        assertEquals("", ultimo.recurso());
    }

    @Test
    @DisplayName("Un registro exige fecha, login, rol y operación")
    void obligatorios() {
        LocalDateTime f = LUNES.atStartOfDay();
        Permiso p = Permiso.CONSULTAR_PERFIL;
        assertThrows(IllegalArgumentException.class, () -> new RegistroAuditoria(1, null, "a", Rol.MEDICO, p, "", true));
        assertThrows(IllegalArgumentException.class, () -> new RegistroAuditoria(1, f, " ", Rol.MEDICO, p, "", true));
        assertThrows(IllegalArgumentException.class, () -> new RegistroAuditoria(1, f, "a", null, p, "", true));
        assertThrows(IllegalArgumentException.class, () -> new RegistroAuditoria(1, f, "a", Rol.MEDICO, null, "", true));
    }
}
