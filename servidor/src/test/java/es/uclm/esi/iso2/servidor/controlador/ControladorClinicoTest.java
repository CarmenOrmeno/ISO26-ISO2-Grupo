package es.uclm.esi.iso2.servidor.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import es.uclm.esi.iso2.servidor.api.ApiInformes;
import es.uclm.esi.iso2.servidor.api.ApiRealizacion;
import es.uclm.esi.iso2.servidor.catalogo.Modalidad;
import es.uclm.esi.iso2.servidor.catalogo.Prioridad;
import es.uclm.esi.iso2.servidor.citas.EstadoCita;
import es.uclm.esi.iso2.servidor.comun.AccesoDenegadoException;
import es.uclm.esi.iso2.servidor.comun.NoEncontradoException;
import es.uclm.esi.iso2.servidor.comun.ReglaNegocioException;
import es.uclm.esi.iso2.servidor.notificaciones.TipoNotificacion;
import es.uclm.esi.iso2.servidor.realizacion.TipoIncidencia;
import es.uclm.esi.iso2.servidor.solicitudes.EstadoSolicitud;

/** It8 · CU08 Registrar realización e incidencias y CU09 Informes e imágenes. */
class ControladorClinicoTest {

    private final Entorno e = new Entorno().conDatosBasicos();
    private final ApiRealizacion realizacion = e.servidor.realizacion();
    private final ApiInformes informes = e.servidor.informes();
    private long solicitud;
    private long cita;

    @BeforeEach
    void preparar() {
        // medico2 solicita; la agenda es de medico1, que realiza
        solicitud = e.solicitar("medico2", "PAC-1", "RX-T", Prioridad.ORDINARIO);
        cita = e.citar(solicitud, e.agenda(Modalidad.RX, "RX1"), "09:00");
    }

    private void realizar() {
        e.reloj.fijar(Entorno.MANANA.atTime(9, 5));
        realizacion.registrarRealizacion(e.token("medico1"), cita, 2, "Sin incidencias");
    }

    @Test
    @DisplayName("CU08: el médico de la agenda registra la realización; estudio en el PACS")
    void registrarRealizacion() {
        realizar();
        String adm = e.token("admision1");
        assertEquals(EstadoCita.REALIZADA, e.servidor.citas().obtener(adm, cita).getEstado());
        assertEquals(EstadoSolicitud.REALIZADA, e.servidor.solicitudes().obtener(adm, solicitud).getEstado());
        assertEquals(1, e.servidor.pacs().imagenes(solicitud).size());
        assertThrows(ReglaNegocioException.class,
                () -> realizacion.registrarRealizacion(e.token("medico1"), cita, 1, "otra vez"));
    }

    @Test
    @DisplayName("CU08: valor límite, antes de la hora de la cita no se registra; en la hora sí")
    void antesDeLaHora() {
        e.reloj.fijar(Entorno.MANANA.atTime(8, 59));
        assertThrows(ReglaNegocioException.class,
                () -> realizacion.registrarRealizacion(e.token("medico1"), cita, 1, ""));
        e.reloj.fijar(Entorno.MANANA.atTime(9, 0));
        realizacion.registrarRealizacion(e.token("medico1"), cita, 1, "");
    }

    @Test
    @DisplayName("CU08: PACS caído, no se registra nada (la cita sigue activa)")
    void pacsCaido() {
        e.servidor.pacs().setDisponible(false);
        e.reloj.fijar(Entorno.MANANA.atTime(9, 5));
        assertThrows(ReglaNegocioException.class,
                () -> realizacion.registrarRealizacion(e.token("medico1"), cita, 1, ""));
        assertEquals(EstadoCita.PROGRAMADA, e.servidor.citas().obtener(e.token("admision1"), cita).getEstado());
    }

    @Test
    @DisplayName("CU08: incidencias; un médico que no es el de la agenda no puede registrarlas")
    void incidencias() {
        String t = e.token("medico1");
        realizacion.registrarIncidencia(t, cita, TipoIncidencia.TECNICA, "Fallo del detector");
        assertEquals(1, realizacion.incidenciasDeCita(t, cita).size());
        assertTrue(realizacion.incidenciasDeCita(t, cita).get(0).toString().contains("TECNICA"));
        assertThrows(IllegalArgumentException.class,
                () -> realizacion.registrarIncidencia(t, cita, TipoIncidencia.OTRA, " "));
        assertThrows(AccesoDenegadoException.class,
                () -> realizacion.registrarIncidencia(e.token("medico2"), cita, TipoIncidencia.OTRA, "x"));
    }

    @Test
    @DisplayName("CU09: crear informe, la solicitud pasa a INFORMADA y el paciente recibe aviso")
    void crearInforme() {
        realizar();
        String t = e.token("medico1");
        informes.crearInforme(t, solicitud, "Campos pulmonares limpios", "Sin hallazgos patológicos");
        assertEquals(EstadoSolicitud.INFORMADA,
                e.servidor.solicitudes().obtener(e.token("admision1"), solicitud).getEstado());
        assertEquals("Sin hallazgos patológicos", informes.informeDeSolicitud(t, solicitud).conclusion());
        assertEquals(1, informes.informesDePaciente(e.token("medico2"), "PAC-1").size());
        assertEquals(1, informes.imagenes(t, solicitud).size());
        assertEquals(TipoNotificacion.INFORME_DISPONIBLE,
                e.servidor.notificaciones().misNotificaciones(e.token("paciente1")).get(0).getTipo());
        assertThrows(ReglaNegocioException.class, () -> informes.crearInforme(t, solicitud, "h", "c"));
    }

    @Test
    @DisplayName("CU09: no se informa una prueba sin realizar; consultar un informe que no existe")
    void sinRealizar() {
        String t = e.token("medico1");
        assertThrows(ReglaNegocioException.class, () -> informes.crearInforme(t, solicitud, "h", "c"));
        assertThrows(NoEncontradoException.class, () -> informes.informeDeSolicitud(t, solicitud));
        assertTrue(informes.imagenes(t, solicitud).isEmpty());
    }

    @Test
    @DisplayName("RNF-S3: un médico que no atiende al paciente no ve sus informes ni imágenes")
    void medicoAjeno() {
        realizar();
        e.servidor.registrarUsuario("medico9", Entorno.CLAVE, "Dr. Ajeno", es.uclm.esi.iso2.servidor.seguridad.Rol.MEDICO, null);
        String t = e.token("medico9");
        assertThrows(AccesoDenegadoException.class, () -> informes.imagenes(t, solicitud));
        assertThrows(AccesoDenegadoException.class, () -> informes.informesDePaciente(t, "PAC-1"));
        assertThrows(AccesoDenegadoException.class, () -> informes.crearInforme(t, solicitud, "h", "c"));
        assertThrows(AccesoDenegadoException.class, () -> informes.informeDeSolicitud(t, solicitud));
    }

    @ParameterizedTest(name = "{0} no registra realizaciones ni informes")
    @ValueSource(strings = { "paciente1", "admision1", "admin1" })
    void sinPermiso(String login) {
        String t = e.token(login);
        assertThrows(AccesoDenegadoException.class, () -> realizacion.registrarRealizacion(t, cita, 1, ""));
        assertThrows(AccesoDenegadoException.class, () -> informes.crearInforme(t, solicitud, "h", "c"));
        assertThrows(AccesoDenegadoException.class, () -> informes.imagenes(t, solicitud));
    }
}
