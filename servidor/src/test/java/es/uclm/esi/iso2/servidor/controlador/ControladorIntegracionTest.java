package es.uclm.esi.iso2.servidor.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import es.uclm.esi.iso2.servidor.Simulados;
import es.uclm.esi.iso2.servidor.api.ApiIntegracion;
import es.uclm.esi.iso2.servidor.catalogo.Modalidad;
import es.uclm.esi.iso2.servidor.catalogo.Prioridad;
import es.uclm.esi.iso2.servidor.comun.AccesoDenegadoException;
import es.uclm.esi.iso2.servidor.comun.ReglaNegocioException;
import es.uclm.esi.iso2.servidor.integracion.DocumentoHce;
import es.uclm.esi.iso2.servidor.integracion.PacienteAdmision;
import es.uclm.esi.iso2.servidor.sincronizacion.EstadoSistema;
import es.uclm.esi.iso2.servidor.sincronizacion.Sincronizacion;
import es.uclm.esi.iso2.servidor.sincronizacion.TipoSincronizacion;

/** It12 · CU19 Integrar con SSO, HCE, admisión y RIS/PACS. */
class ControladorIntegracionTest {

    private final Entorno e = new Entorno().conDatosBasicos();
    private final ApiIntegracion integracion = e.servidor.integracion();

    /** Sesión nueva en cada uso: algunas pruebas adelantan el reloj más de 30 min (caducidad de CU01). */
    private String admin() {
        return e.token("admin1");
    }

    private static PacienteAdmision enAdmision(String dni, String nombre) {
        return new PacienteAdmision(dni, nombre, "Apellidos", LocalDate.of(1990, 1, 1), "926000000", null);
    }

    /** Eco de PAC-1 realizada e informada por medico1. Devuelve el id de la solicitud. */
    private long informar() {
        long eco = e.solicitar("medico1", "PAC-1", "US-A", Prioridad.ORDINARIO);
        long cita = e.citar(eco, e.agenda(Modalidad.US, "ECO1"), "09:00");
        e.reloj.fijar(Entorno.MANANA.atTime(9, 30));
        e.servidor.realizacion().registrarRealizacion(e.token("medico1"), cita, 1, "");
        e.servidor.informes().crearInforme(e.token("medico1"), eco, "Hígado homogéneo", "Sin alteraciones");
        return eco;
    }

    @Test
    @DisplayName("CU19: estado de los cuatro sistemas externos; un sistema caído aparece como no disponible")
    void estado() {
        assertEquals(List.of("SSO corporativo", "HCE", "Admisión", "RIS/PACS"),
                integracion.estado(admin()).stream().map(EstadoSistema::nombre).toList());
        assertTrue(integracion.estado(admin()).stream().allMatch(EstadoSistema::disponible));
        Simulados.pacs(e.servidor).setDisponible(false);
        EstadoSistema pacs = integracion.estado(admin()).get(3);
        assertFalse(pacs.disponible());
        assertEquals("RIS/PACS: NO disponible", pacs.toString());
    }

    @Test
    @DisplayName("CU19: con el SSO caído nadie puede iniciar sesión; las sesiones abiertas siguen")
    void ssoCaido() {
        String abierta = admin();
        Simulados.sso(e.servidor).setDisponible(false);
        assertThrows(ReglaNegocioException.class, () -> e.token("medico1"));
        assertFalse(integracion.estado(abierta).get(0).disponible());
    }

    @Test
    @DisplayName("CU19: importa de admisión los pacientes nuevos; omite los que ya existen y rechaza los inválidos")
    void importar() {
        Simulados.admision(e.servidor).registrar(enAdmision("11223344B", "Rosa"))      // nuevo
                .registrar(enAdmision("12345678Z", "Lucía"))                  // ya es PAC-1
                .registrar(enAdmision("12345678A", "Erróneo"));               // letra del DNI incorrecta
        Sincronizacion s = integracion.importarPacientes(admin());
        assertEquals(TipoSincronizacion.IMPORTAR_PACIENTES, s.tipo());
        assertEquals("Admisión", s.sistema());
        assertEquals(1, s.procesados());
        assertEquals(1, s.omitidos());
        assertEquals(1, s.rechazados());
        assertFalse(s.correcta());
        assertTrue(s.detalle().startsWith("12345678A"));
        assertEquals("Rosa", e.servidor.pacientes().buscarPorDni(e.token("admision1"), "11223344B").getNombre());

        // repetir no duplica: el nuevo ya existe
        Sincronizacion otra = integracion.importarPacientes(admin());
        assertEquals(0, otra.procesados());
        assertEquals(2, otra.omitidos());
    }

    @Test
    @DisplayName("CU19: con admisión caída la importación queda guardada como fallida y no cambia nada")
    void admisionCaida() {
        Simulados.admision(e.servidor).registrar(enAdmision("11223344B", "Rosa")).setDisponible(false);
        Sincronizacion s = integracion.importarPacientes(admin());
        assertFalse(s.correcta());
        assertEquals(0, s.procesados());
        assertTrue(s.detalle().contains("no está disponible"));
        assertEquals(2, e.servidor.pacientes().listarActivos(e.token("admision1")).size());
        assertEquals(List.of(s), integracion.sincronizaciones(admin()));
    }

    @Test
    @DisplayName("CU19: publica en la HCE los informes no enviados, cada uno una sola vez")
    void enviarInformes() {
        long eco = informar();
        Sincronizacion s = integracion.enviarInformesHce(admin());
        assertTrue(s.correcta());
        assertEquals(1, s.procesados());
        List<DocumentoHce> enHce = Simulados.hce(e.servidor).documentos("12345678Z");
        assertEquals(1, enHce.size());
        assertTrue(enHce.get(0).titulo().endsWith("solicitud " + eco));
        assertTrue(enHce.get(0).texto().contains("Sin alteraciones"));

        Sincronizacion otra = integracion.enviarInformesHce(admin());
        assertEquals(0, otra.procesados());
        assertEquals(1, otra.omitidos());
        assertEquals(1, Simulados.hce(e.servidor).documentos("12345678Z").size());
    }

    @Test
    @DisplayName("CU19: con la HCE caída no se pierde nada: se envía en la siguiente sincronización")
    void hceCaida() {
        informar();
        Simulados.hce(e.servidor).setDisponible(false);
        Sincronizacion fallida = integracion.enviarInformesHce(admin());
        assertFalse(fallida.correcta());
        assertTrue(fallida.detalle().contains("1 informes pendientes"));
        Simulados.hce(e.servidor).setDisponible(true);
        Sincronizacion buena = integracion.enviarInformesHce(admin());
        assertEquals(1, buena.procesados());
        assertEquals(List.of(buena.id(), fallida.id()),
                integracion.sincronizaciones(admin()).stream().map(Sincronizacion::id).toList());
    }

    @Test
    @DisplayName("CU19: las sincronizaciones mueven datos clínicos y quedan auditadas (CU18)")
    void auditada() {
        integracion.importarPacientes(admin());
        assertEquals("importar pacientes de admisión", e.servidor.auditoria()
                .registrosDeUsuario(admin(), "admin1", Entorno.INICIO.toLocalDate(), Entorno.INICIO.toLocalDate())
                .get(0).recurso());
    }

    @ParameterizedTest(name = "{0} no gestiona integraciones")
    @ValueSource(strings = { "paciente1", "medico1", "admision1" })
    void sinPermiso(String login) {
        String t = e.token(login);
        assertThrows(AccesoDenegadoException.class, () -> integracion.estado(t));
        assertThrows(AccesoDenegadoException.class, () -> integracion.importarPacientes(t));
        assertThrows(AccesoDenegadoException.class, () -> integracion.enviarInformesHce(t));
        assertThrows(AccesoDenegadoException.class, () -> integracion.sincronizaciones(t));
    }
}
