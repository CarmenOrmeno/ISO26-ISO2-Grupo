package es.uclm.esi.iso2.servidor.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import es.uclm.esi.iso2.servidor.agendas.DatosAgenda;
import es.uclm.esi.iso2.servidor.api.ApiInformesOperativos;
import es.uclm.esi.iso2.servidor.catalogo.Modalidad;
import es.uclm.esi.iso2.servidor.catalogo.Prioridad;
import es.uclm.esi.iso2.servidor.comun.AccesoDenegadoException;
import es.uclm.esi.iso2.servidor.estadisticas.EsperaPrioridad;
import es.uclm.esi.iso2.servidor.estadisticas.InformeOperativo;
import es.uclm.esi.iso2.servidor.estadisticas.OcupacionModalidad;

/** It13 · CU21 Generar informes operativos. */
class ControladorInformesOperativosTest {

    private static final LocalDate HOY = Entorno.INICIO.toLocalDate();
    private static final LocalDate MANANA = Entorno.MANANA;
    private static final LocalDate PASADO = MANANA.plusDays(1);

    private final Entorno e = new Entorno().conDatosBasicos();
    private final ApiInformesOperativos informes = e.servidor.informesOperativos();

    /**
     * Solicitudes de hoy. Mañana (RX, 4 huecos): Rx ORDINARIO a las 9:00 y Rx URGENTE a las 9:15 (1 día =
     * plazo URGENTE). Pasado mañana (TC, 4 huecos): TAC URGENTE (2 días: fuera de plazo). Mañana (US,
     * 4 huecos): eco PREFERENTE, cancelada.
     */
    @BeforeEach
    void preparar() {
        long rxOrdinaria = e.solicitar("medico1", "PAC-1", "RX-T", Prioridad.ORDINARIO);
        long rxUrgente = e.solicitar("medico1", "PAC-2", "RX-T", Prioridad.URGENTE);
        long rxUrgenteTarde = e.solicitar("medico1", "PAC-1", "TC-C", Prioridad.URGENTE);
        long eco = e.solicitar("medico1", "PAC-2", "US-A", Prioridad.PREFERENTE);
        long rxManana = e.agenda(Modalidad.RX, "RX1");
        long tcPasado = e.servidor.agendas().crear(e.token("admision1"), new DatosAgenda(PASADO, "TC1", Modalidad.TC,
                "medico1", LocalTime.of(9, 0), LocalTime.of(10, 0), 15)).getId();
        e.citar(rxOrdinaria, rxManana, "09:00");
        e.citar(rxUrgente, rxManana, "09:15");
        e.citar(rxUrgenteTarde, tcPasado, "09:00");
        long citaEco = e.citar(eco, e.agenda(Modalidad.US, "ECO1", "medico2"), "09:00");
        e.servidor.citas().cancelar(e.token("admision1"), citaEco, "Equipo averiado");
    }

    private InformeOperativo informe(LocalDate desde, LocalDate hasta) {
        return informes.generar(e.token("admision1"), desde, hasta);
    }

    @Test
    @DisplayName("CU21: ocupación por modalidad y total (la cita cancelada libera su hueco)")
    void ocupacion() {
        InformeOperativo i = informe(MANANA, PASADO);
        assertEquals(List.of(new OcupacionModalidad(Modalidad.RX, 4, 2), new OcupacionModalidad(Modalidad.US, 4, 0),
                new OcupacionModalidad(Modalidad.TC, 4, 1)), i.ocupacion());
        assertEquals(12, i.huecos());
        assertEquals(3, i.huecosOcupados());
        assertEquals(25.0, i.ocupacionTotal().getAsDouble());
        assertEquals(50.0, i.ocupacion().get(0).porcentaje().getAsDouble());
    }

    @Test
    @DisplayName("CU21: espera hasta la cita por prioridad; valor límite del plazo (1 día URGENTE dentro, 2 fuera)")
    void esperas() {
        List<EsperaPrioridad> esperas = informe(MANANA, PASADO).esperas();
        assertEquals(List.of(new EsperaPrioridad(Prioridad.URGENTE, 2, 1.5, 1, 1),
                new EsperaPrioridad(Prioridad.ORDINARIO, 1, 1.0, 90, 0)), esperas);
    }

    @Test
    @DisplayName("CU21: cancelaciones del periodo y tasa de cancelación")
    void cancelaciones() {
        InformeOperativo i = informe(MANANA, PASADO);
        assertEquals(4, i.citasDelPeriodo());
        assertEquals(1, i.cancelaciones());
        assertEquals(25.0, i.tasaCancelacion().getAsDouble());
        assertTrue(i.toString().contains("Cancelaciones: 1 de 4 citas (25.0 %)"));
    }

    @Test
    @DisplayName("CU21: periodo sin agendas ni citas → informe vacío, porcentajes «sin datos»")
    void vacio() {
        InformeOperativo i = informe(HOY, HOY);
        assertTrue(i.ocupacion().isEmpty());
        assertTrue(i.esperas().isEmpty());
        assertTrue(i.ocupacionTotal().isEmpty());
        assertTrue(i.tasaCancelacion().isEmpty());
        assertTrue(i.toString().contains("sin datos"));
    }

    @Test
    @DisplayName("CU21: solo cuenta lo del periodo (un día) y rechaza hasta < desde")
    void periodo() {
        InformeOperativo soloManana = informe(MANANA, MANANA);
        assertEquals(3, soloManana.citasDelPeriodo());
        assertEquals(8, soloManana.huecos());
        assertThrows(IllegalArgumentException.class, () -> informe(PASADO, MANANA));
    }

    @ParameterizedTest(name = "{0} no genera informes operativos")
    @ValueSource(strings = { "paciente1", "medico1", "admin1" })
    void sinPermiso(String login) {
        String t = e.token(login);
        assertThrows(AccesoDenegadoException.class, () -> informes.generar(t, MANANA, PASADO));
    }
}
