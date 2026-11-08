package es.uclm.esi.iso2.servidor.agendas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import es.uclm.esi.iso2.servidor.catalogo.Modalidad;
import es.uclm.esi.iso2.servidor.catalogo.ParametrosSistema;
import es.uclm.esi.iso2.servidor.comun.NoEncontradoException;
import es.uclm.esi.iso2.servidor.comun.ReglaNegocioException;
import es.uclm.esi.iso2.servidor.comun.RelojAjustable;
import es.uclm.esi.iso2.servidor.comun.RepositorioEnMemoria;

class ServicioAgendasImplTest {

    static final LocalDate HOY = LocalDate.of(2026, 11, 2);
    private final ServicioAgendas servicio = new ServicioAgendasImpl(new RepositorioEnMemoria<>(Agenda::getId),
            new ParametrosSistema(), new RelojAjustable(LocalDateTime.of(2026, 11, 2, 9, 0)));

    static DatosAgenda datos(LocalDate dia, String sala, String medico, String inicio, String fin) {
        return new DatosAgenda(dia, sala, Modalidad.RX, medico, LocalTime.parse(inicio), LocalTime.parse(fin), 30);
    }

    @ParameterizedTest(name = "fecha hoy{0} días → válida={1}")
    @CsvSource({ "-1,false", "0,true", "1,true" })
    @DisplayName("Valores límite de la fecha: no se crean agendas en el pasado")
    void fecha(int dias, boolean valida) {
        DatosAgenda d = datos(HOY.plusDays(dias), "RX1", "m", "09:00", "10:00");
        if (valida) {
            assertEquals(1, servicio.crear(d).getId());
        } else {
            assertThrows(ReglaNegocioException.class, () -> servicio.crear(d));
        }
    }

    @ParameterizedTest(name = "{0}-{1} con horario 08:00-21:00 → válida={2}")
    @CsvSource({ "07:30,09:00,false", "08:00,09:00,true", "20:00,21:00,true", "20:00,21:30,false" })
    void horario(String inicio, String fin, boolean valida) {
        DatosAgenda d = datos(HOY, "RX1", "m", inicio, fin);
        if (valida) {
            servicio.crear(d);
        } else {
            assertThrows(ReglaNegocioException.class, () -> servicio.crear(d));
        }
    }

    @Test
    @DisplayName("Misma sala o mismo médico solapados se rechazan; consecutivas u otro día no")
    void solapamientos() {
        servicio.crear(datos(HOY, "RX1", "m1", "09:00", "11:00"));
        assertThrows(ReglaNegocioException.class, () -> servicio.crear(datos(HOY, "RX1", "m2", "10:00", "12:00")));
        assertThrows(ReglaNegocioException.class, () -> servicio.crear(datos(HOY, "RX2", "m1", "10:30", "12:00")));
        servicio.crear(datos(HOY, "RX1", "m2", "11:00", "12:00"));
        servicio.crear(datos(HOY.plusDays(1), "RX1", "m1", "09:00", "11:00"));
        servicio.crear(datos(HOY, "RX2", "m3", "09:00", "11:00"));
        assertEquals(3, servicio.delDia(HOY).size());
        assertEquals(LocalTime.of(9, 0), servicio.delDia(HOY).get(0).getInicio());
        assertEquals(4, servicio.todas().size());
    }

    @Test
    @DisplayName("Huecos libres por modalidad y rango de fechas, ordenados; ocupar y liberar")
    void huecosLibres() {
        long a2 = servicio.crear(datos(HOY.plusDays(1), "RX1", "m1", "09:00", "10:00")).getId();
        long a1 = servicio.crear(datos(HOY, "RX1", "m1", "12:00", "13:00")).getId();
        servicio.crear(new DatosAgenda(HOY, "TC1", Modalidad.TC, "m2", LocalTime.of(9, 0), LocalTime.of(10, 0), 30));
        List<HuecoLibre> libres = servicio.huecosLibres(Modalidad.RX, HOY, HOY.plusDays(1));
        assertEquals(4, libres.size());
        assertEquals(a1, libres.get(0).idAgenda());
        assertEquals(2, servicio.huecosLibres(Modalidad.RX, HOY, HOY).size());
        servicio.ocupar(a2, LocalTime.of(9, 0), 1);
        assertEquals(3, servicio.huecosLibres(Modalidad.RX, HOY, HOY.plusDays(1)).size());
        servicio.liberar(a2, LocalTime.of(9, 0));
        assertEquals(4, servicio.huecosLibres(Modalidad.RX, HOY, HOY.plusDays(5)).size());
    }

    @Test
    @DisplayName("Eliminar: solo sin citas; inexistente → NoEncontrado")
    void eliminar() {
        long id = servicio.crear(datos(HOY, "RX1", "m1", "09:00", "10:00")).getId();
        servicio.ocupar(id, LocalTime.of(9, 30), 5);
        assertThrows(ReglaNegocioException.class, () -> servicio.eliminar(id));
        servicio.liberar(id, LocalTime.of(9, 30));
        servicio.eliminar(id);
        assertThrows(NoEncontradoException.class, () -> servicio.obtener(id));
        assertThrows(NoEncontradoException.class, () -> servicio.eliminar(99));
    }
}
