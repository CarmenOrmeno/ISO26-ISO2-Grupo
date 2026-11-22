package es.uclm.esi.iso2.servidor.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import es.uclm.esi.iso2.servidor.agendas.DatosAgenda;
import es.uclm.esi.iso2.servidor.api.ApiEsperas;
import es.uclm.esi.iso2.servidor.catalogo.Modalidad;
import es.uclm.esi.iso2.servidor.catalogo.Prioridad;
import es.uclm.esi.iso2.servidor.citas.Cita;
import es.uclm.esi.iso2.servidor.comun.AccesoDenegadoException;
import es.uclm.esi.iso2.servidor.comun.NoEncontradoException;
import es.uclm.esi.iso2.servidor.comun.ReglaNegocioException;
import es.uclm.esi.iso2.servidor.esperas.EntradaEspera;

/** It10 · CU11 Gestionar listas de espera, prioridades y huecos. */
class ControladorEsperasTest {

    private static final LocalDate HOY = Entorno.INICIO.toLocalDate();

    private final Entorno e = new Entorno().conDatosBasicos();
    private final ApiEsperas esperas = e.servidor.esperas();

    private String adm() {
        return e.token("admision1");
    }

    private List<Long> ids(List<EntradaEspera> lista) {
        return lista.stream().map(EntradaEspera::idSolicitud).toList();
    }

    private long agendaEl(LocalDate dia, Modalidad modalidad, String sala) {
        return e.servidor.agendas().crear(adm(), new DatosAgenda(dia, sala, modalidad, "medico1",
                LocalTime.of(9, 0), LocalTime.of(10, 0), 15)).getId();
    }

    @Test
    @DisplayName("CU11: la lista se ordena por prioridad y, a igual prioridad, por antigüedad")
    void orden() {
        long ordinariaAntigua = e.solicitar("medico1", "PAC-1", "RX-T", Prioridad.ORDINARIO);
        e.reloj.avanzar(Duration.ofDays(1));
        long ordinariaReciente = e.solicitar("medico1", "PAC-2", "RX-T", Prioridad.ORDINARIO);
        long preferente = e.solicitar("medico1", "PAC-1", "US-A", Prioridad.PREFERENTE);
        long urgente = e.solicitar("medico2", "PAC-2", "TC-C", Prioridad.URGENTE);

        assertEquals(List.of(urgente, preferente, ordinariaAntigua, ordinariaReciente),
                ids(esperas.listaDeEspera(adm())));
        assertEquals(List.of(ordinariaAntigua, ordinariaReciente), ids(esperas.listaDeEspera(adm(), Modalidad.RX)));
        assertTrue(esperas.listaDeEspera(adm(), Modalidad.MG).isEmpty());
    }

    @Test
    @DisplayName("CU11: valor límite del plazo, el último día está en plazo y el siguiente fuera")
    void plazo() {
        e.solicitar("medico1", "PAC-1", "TC-C", Prioridad.URGENTE);
        EntradaEspera hoy = esperas.listaDeEspera(adm()).get(0);
        assertEquals(HOY.plusDays(1), hoy.fechaLimite());
        assertEquals(0, hoy.diasEspera());

        e.reloj.avanzar(Duration.ofDays(1));
        assertFalse(esperas.listaDeEspera(adm()).get(0).fueraDePlazo());
        e.reloj.avanzar(Duration.ofDays(1));
        EntradaEspera vencida = esperas.listaDeEspera(adm()).get(0);
        assertTrue(vencida.fueraDePlazo());
        assertEquals(2, vencida.diasEspera());
        assertTrue(vencida.toString().contains("FUERA DE PLAZO"));
    }

    @Test
    @DisplayName("CU11: subir la prioridad adelanta la solicitud; no se cambia si ya está citada")
    void cambiarPrioridad() {
        long primera = e.solicitar("medico1", "PAC-1", "US-A", Prioridad.PREFERENTE);
        long segunda = e.solicitar("medico1", "PAC-2", "RX-T", Prioridad.ORDINARIO);
        esperas.cambiarPrioridad(adm(), segunda, Prioridad.URGENTE);
        assertEquals(List.of(segunda, primera), ids(esperas.listaDeEspera(adm())));

        assertThrows(IllegalArgumentException.class, () -> esperas.cambiarPrioridad(adm(), primera, null));
        e.citar(primera, e.agenda(Modalidad.US, "ECO1"), "09:00");
        assertThrows(ReglaNegocioException.class,
                () -> esperas.cambiarPrioridad(adm(), primera, Prioridad.URGENTE));
        assertThrows(NoEncontradoException.class, () -> esperas.cambiarPrioridad(adm(), 99, Prioridad.URGENTE));
    }

    @Test
    @DisplayName("CU11: citar en el primer hueco libre; la solicitud sale de la lista de espera")
    void citarEnPrimerHueco() {
        e.agenda(Modalidad.RX, "RX1");
        long s1 = e.solicitar("medico1", "PAC-1", "RX-T", Prioridad.ORDINARIO);
        long s2 = e.solicitar("medico1", "PAC-2", "RX-T", Prioridad.ORDINARIO);
        assertEquals(Entorno.MANANA.atTime(9, 0), esperas.citarEnPrimerHueco(adm(), s1).getFechaHora());
        assertEquals(Entorno.MANANA.atTime(9, 15), esperas.citarEnPrimerHueco(adm(), s2).getFechaHora());
        assertTrue(esperas.listaDeEspera(adm()).isEmpty());
        assertThrows(ReglaNegocioException.class, () -> esperas.citarEnPrimerHueco(adm(), s1));
    }

    @Test
    @DisplayName("CU11: valor límite de los huecos, el que empieza ahora no sirve; el siguiente sí")
    void huecoQueEmpiezaAhora() {
        e.agenda(Modalidad.RX, "RX1");
        long s = e.solicitar("medico1", "PAC-1", "RX-T", Prioridad.ORDINARIO);
        e.reloj.fijar(Entorno.MANANA.atTime(9, 15));
        assertEquals(Entorno.MANANA.atTime(9, 30), esperas.citarEnPrimerHueco(adm(), s).getFechaHora());
    }

    @Test
    @DisplayName("Conjetura de errores: si el paciente ya tiene cita a esa hora se usa el siguiente hueco")
    void pacienteOcupado() {
        long eco = e.solicitar("medico1", "PAC-1", "US-A", Prioridad.ORDINARIO);
        e.citar(eco, e.agenda(Modalidad.US, "ECO1", "medico2"), "09:00");
        e.agenda(Modalidad.RX, "RX1");
        long rx = e.solicitar("medico1", "PAC-1", "RX-T", Prioridad.ORDINARIO);
        Cita cita = esperas.citarEnPrimerHueco(adm(), rx);
        assertEquals(LocalTime.of(9, 15), cita.getFechaHora().toLocalTime());
    }

    @Test
    @DisplayName("CU11: valor límite, un URGENTE (plazo 1 día) se cita mañana pero no pasado mañana")
    void sinHuecoDentroDelPlazo() {
        agendaEl(HOY.plusDays(2), Modalidad.TC, "TC1");
        long urgente = e.solicitar("medico1", "PAC-1", "TC-C", Prioridad.URGENTE);
        assertThrows(ReglaNegocioException.class, () -> esperas.citarEnPrimerHueco(adm(), urgente));

        agendaEl(HOY.plusDays(1), Modalidad.TC, "TC1");
        assertEquals(HOY.plusDays(1), esperas.citarEnPrimerHueco(adm(), urgente).getFechaHora().toLocalDate());
    }

    @Test
    @DisplayName("CU11: una solicitud fuera de plazo se busca en un nuevo plazo desde hoy")
    void fueraDePlazo() {
        long urgente = e.solicitar("medico1", "PAC-1", "TC-C", Prioridad.URGENTE);
        e.reloj.avanzar(Duration.ofDays(3));
        agendaEl(HOY.plusDays(4), Modalidad.TC, "TC1");
        assertEquals(HOY.plusDays(4), esperas.citarEnPrimerHueco(adm(), urgente).getFechaHora().toLocalDate());
    }

    @Test
    @DisplayName("CU11: rellenar huecos cita en orden las que caben y deja las demás en espera")
    void rellenarHuecos() {
        assertEquals(0, esperas.rellenarHuecos(adm(), Modalidad.RX), "sin solicitudes");
        e.solicitar("medico1", "PAC-1", "RX-T", Prioridad.ORDINARIO);
        e.solicitar("medico1", "PAC-2", "RX-T", Prioridad.URGENTE);
        long eco = e.solicitar("medico1", "PAC-1", "US-A", Prioridad.ORDINARIO);
        assertEquals(0, esperas.rellenarHuecos(adm(), Modalidad.RX), "sin agenda");

        e.agenda(Modalidad.RX, "RX1");
        assertEquals(2, esperas.rellenarHuecos(adm(), Modalidad.RX));
        assertEquals(List.of(eco), ids(esperas.listaDeEspera(adm())));
    }

    @ParameterizedTest(name = "{0} no gestiona listas de espera")
    @ValueSource(strings = { "paciente1", "medico1", "admin1" })
    void sinPermiso(String login) {
        String t = e.token(login);
        assertThrows(AccesoDenegadoException.class, () -> esperas.listaDeEspera(t));
        assertThrows(AccesoDenegadoException.class, () -> esperas.listaDeEspera(t, Modalidad.RX));
        assertThrows(AccesoDenegadoException.class, () -> esperas.cambiarPrioridad(t, 1, Prioridad.URGENTE));
        assertThrows(AccesoDenegadoException.class, () -> esperas.citarEnPrimerHueco(t, 1));
        assertThrows(AccesoDenegadoException.class, () -> esperas.rellenarHuecos(t, Modalidad.RX));
    }
}
