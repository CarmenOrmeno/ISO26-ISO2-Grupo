package es.uclm.esi.iso2.servidor.agendas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import es.uclm.esi.iso2.servidor.catalogo.Modalidad;
import es.uclm.esi.iso2.servidor.comun.NoEncontradoException;
import es.uclm.esi.iso2.servidor.comun.ReglaNegocioException;

class AgendaTest {

    static final LocalDate DIA = LocalDate.of(2026, 11, 3);

    static DatosAgenda datos(String inicio, String fin, int minutos) {
        return new DatosAgenda(DIA, "RX1", Modalidad.RX, "medico1", LocalTime.parse(inicio), LocalTime.parse(fin), minutos);
    }

    @ParameterizedTest(name = "{0}-{1} cada {2} min → {3} huecos")
    @CsvSource({ "09:00,10:00,15,4", "09:00,10:00,20,3", "09:00,10:10,20,3", "09:00,09:05,5,1", "09:00,11:00,120,1" })
    @DisplayName("Generación de huecos (el resto que no llega a un hueco se descarta)")
    void huecos(String inicio, String fin, int minutos, int esperados) {
        assertEquals(esperados, new Agenda(1, datos(inicio, fin, minutos)).getHuecos().size());
    }

    @ParameterizedTest(name = "{0} min por hueco → válido={1}")
    @CsvSource({ "4,false", "5,true", "120,true", "121,false" })
    @DisplayName("Valores límite de minutos por hueco [5, 120]")
    void limitesMinutos(int minutos, boolean valido) {
        if (valido) {
            new Agenda(1, datos("08:00", "12:00", minutos));
        } else {
            assertThrows(IllegalArgumentException.class, () -> new Agenda(1, datos("08:00", "12:00", minutos)));
        }
    }

    @Test
    @DisplayName("Una agenda más corta que un hueco (o con fin antes que inicio) es inválida")
    void demasiadoCorta() {
        assertThrows(IllegalArgumentException.class, () -> new Agenda(1, datos("09:00", "09:14", 15)));
        assertThrows(IllegalArgumentException.class, () -> new Agenda(1, datos("10:00", "09:00", 15)));
    }

    @Test
    @DisplayName("Campos obligatorios")
    void obligatorios() {
        assertThrows(IllegalArgumentException.class, () -> new Agenda(1, null));
        assertThrows(IllegalArgumentException.class, () -> new Agenda(1,
                new DatosAgenda(null, "S", Modalidad.RX, "m", LocalTime.NOON, LocalTime.MIDNIGHT.minusMinutes(1), 15)));
        assertThrows(IllegalArgumentException.class, () -> new Agenda(1,
                new DatosAgenda(DIA, "S", null, "m", LocalTime.of(9, 0), LocalTime.of(10, 0), 15)));
        assertThrows(IllegalArgumentException.class, () -> new Agenda(1,
                new DatosAgenda(DIA, "S", Modalidad.RX, "", LocalTime.of(9, 0), LocalTime.of(10, 0), 15)));
    }

    @ParameterizedTest(name = "[{0}, {1}) solapa con 09:00-10:00 → {2}")
    @CsvSource({ "08:00,09:00,false", "08:00,09:01,true", "09:30,09:45,true", "09:59,11:00,true", "10:00,11:00,false" })
    @DisplayName("Valores límite del solapamiento (intervalos semiabiertos)")
    void solapamiento(LocalTime desde, LocalTime hasta, boolean solapa) {
        Agenda a = new Agenda(1, datos("09:00", "10:00", 15));
        assertEquals(solapa, a.seSolapa(DIA, desde, hasta));
        assertFalse(a.seSolapa(DIA.plusDays(1), desde, hasta));
    }

    @Test
    @DisplayName("Ocupar y liberar huecos; ocupar dos veces o una hora sin hueco falla")
    void ocuparLiberar() {
        Agenda a = new Agenda(1, datos("09:00", "10:00", 15));
        a.ocupar(LocalTime.of(9, 15), 7);
        assertEquals(1, a.huecosOcupados());
        assertEquals(3, a.huecosLibres().size());
        assertEquals(7L, a.hueco(LocalTime.of(9, 15)).getIdCita());
        assertTrue(a.hueco(LocalTime.of(9, 15)).toString().contains("cita 7"));
        assertThrows(ReglaNegocioException.class, () -> a.ocupar(LocalTime.of(9, 15), 8));
        assertThrows(NoEncontradoException.class, () -> a.ocupar(LocalTime.of(9, 10), 8));
        a.liberar(LocalTime.of(9, 15));
        assertTrue(a.hueco(LocalTime.of(9, 15)).libre());
        assertTrue(a.toString().contains("0/4 ocupados"));
    }

    @Test
    void huecoLibre() {
        HuecoLibre h = new Agenda(3, datos("09:00", "10:00", 30)).huecosLibres().get(1);
        assertEquals(DIA.atTime(9, 30), h.momento());
        assertTrue(h.toString().contains("agenda 3"));
    }
}
