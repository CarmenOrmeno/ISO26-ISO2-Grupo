package es.uclm.esi.iso2.servidor.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import es.uclm.esi.iso2.servidor.api.ApiAuditoria;
import es.uclm.esi.iso2.servidor.auditoria.RegistroAuditoria;
import es.uclm.esi.iso2.servidor.catalogo.Modalidad;
import es.uclm.esi.iso2.servidor.catalogo.Prioridad;
import es.uclm.esi.iso2.servidor.comun.AccesoDenegadoException;
import es.uclm.esi.iso2.servidor.seguridad.Permiso;
import es.uclm.esi.iso2.servidor.seguridad.Rol;

/** It12 · CU18 Auditar accesos: el servidor completo deja constancia de los accesos (RNF-S2). */
class ControladorAuditoriaTest {

    private static final LocalDate HOY = Entorno.INICIO.toLocalDate();
    private static final LocalDate MANANA = Entorno.MANANA;

    private final Entorno e = new Entorno().conDatosBasicos();
    private final ApiAuditoria auditoria = e.servidor.auditoria();
    private long eco;

    /** Eco de PAC-1 realizada e informada mañana por medico2. */
    @BeforeEach
    void preparar() {
        eco = e.solicitar("medico2", "PAC-1", "US-A", Prioridad.ORDINARIO);
        long cita = e.citar(eco, e.agenda(Modalidad.US, "ECO1", "medico2"), "09:00");
        e.reloj.fijar(MANANA.atTime(9, 30));
        e.servidor.realizacion().registrarRealizacion(e.token("medico2"), cita, 1, "");
        e.servidor.informes().crearInforme(e.token("medico2"), eco, "Hallazgos", "Normal");
    }

    private List<RegistroAuditoria> todos() {
        return auditoria.registros(e.token("admin1"), HOY, MANANA);
    }

    @Test
    @DisplayName("CU18: el acceso a un informe queda auditado con usuario, operación, recurso y fecha")
    void informeAuditado() {
        e.reloj.fijar(MANANA.atTime(12, 0));
        e.servidor.informes().informeDeSolicitud(e.token("medico2"), eco);
        RegistroAuditoria r = todos().get(0);
        assertEquals("medico2", r.login());
        assertEquals(Rol.MEDICO, r.rol());
        assertEquals(Permiso.CONSULTAR_INFORMES_PACIENTES, r.operacion());
        assertEquals("informe de la solicitud " + eco, r.recurso());
        assertEquals(MANANA.atTime(12, 0), r.fecha());
        assertTrue(r.permitido());
    }

    @Test
    @DisplayName("CU18: el historial y el informe que consulta el paciente quedan auditados")
    void pacienteAuditado() {
        String t = e.token("paciente1");
        e.servidor.historial().miHistorial(t);
        e.servidor.historial().miInforme(t, eco);
        List<RegistroAuditoria> suyos = auditoria.registrosDeUsuario(e.token("admin1"), "paciente1", HOY, MANANA);
        assertEquals(List.of("informe de la solicitud " + eco, "historial propio"),
                suyos.stream().map(RegistroAuditoria::recurso).toList());
    }

    @Test
    @DisplayName("CU18: el intento de un rol sin permiso queda auditado como denegado")
    void denegadoAuditado() {
        String admision = e.token("admision1");
        assertThrows(AccesoDenegadoException.class, () -> e.servidor.informes().imagenes(admision, eco));
        List<RegistroAuditoria> denegados = auditoria.accesosDenegados(e.token("admin1"), HOY, MANANA);
        assertEquals(1, denegados.size());
        assertEquals("admision1", denegados.get(0).login());
        assertEquals("imágenes de la solicitud " + eco, denegados.get(0).recurso());
        assertFalse(denegados.get(0).permitido());
    }

    @Test
    @DisplayName("CU18: quién ha accedido a una prueba (búsqueda por recurso)")
    void porRecurso() {
        e.servidor.informes().imagenes(e.token("medico2"), eco);
        List<RegistroAuditoria> sobreEco = auditoria.accesosARecurso(e.token("admin1"), "solicitud " + eco, HOY, MANANA);
        // crear el informe y ver sus imágenes
        assertEquals(List.of(Permiso.CONSULTAR_INFORMES_PACIENTES, Permiso.CREAR_INFORMES),
                sobreEco.stream().map(RegistroAuditoria::operacion).toList());
    }

    @Test
    @DisplayName("CU18: las operaciones sin datos clínicos permitidas no se auditan")
    void sinDatosClinicos() {
        int antes = todos().size();
        e.servidor.catalogo().listar(e.token("admision1"));
        assertEquals(antes, todos().size());
    }

    @ParameterizedTest(name = "{0} no consulta la auditoría")
    @ValueSource(strings = { "paciente1", "medico1", "admision1" })
    void sinPermiso(String login) {
        String t = e.token(login);
        assertThrows(AccesoDenegadoException.class, () -> auditoria.registros(t, HOY, MANANA));
        assertThrows(AccesoDenegadoException.class, () -> auditoria.accesosDenegados(t, HOY, MANANA));
    }
}
