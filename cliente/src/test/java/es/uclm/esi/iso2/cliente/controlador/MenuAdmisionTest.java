package es.uclm.esi.iso2.cliente.controlador;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.Clock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.uclm.esi.iso2.cliente.vista.Guion;
import es.uclm.esi.iso2.servidor.Servidor;

/** Pruebas de sistema del menú de admisión con el servidor de demostración (lunes 02/11/2026). */
class MenuAdmisionTest {

    private static final Clock RELOJ = Clock.fixed(LocalDateTime.of(2026, 11, 2, 8, 0).toInstant(ZoneOffset.UTC),
            ZoneOffset.UTC);
    private static final LocalDate MANANA = LocalDate.of(2026, 11, 3);

    /** Inicia sesión como admision1, ejecuta las líneas y sale. */
    private String admision(String... lineas) {
        String[] todo = new String[lineas.length + 4];
        todo[0] = "admision1";
        todo[1] = "demo";
        System.arraycopy(lineas, 0, todo, 2, lineas.length);
        todo[todo.length - 2] = "0";
        todo[todo.length - 1] = "";
        Guion g = new Guion(todo);
        FabricaMenus.crear(g.consola(), Servidor.conDatosDemo(RELOJ)).ejecutar();
        return g.salida();
    }

    @Test
    @DisplayName("CU02: alta, búsqueda por DNI, listado, modificación y baja de paciente")
    void pacientes() {
        String salida = admision(
                "2", "11111111H", "Ana", "Ruiz", "1990-01-01", "611222333", "",
                "3", "11111111H",
                "4",
                "5", "PAC-3", "11111111H", "Ana", "Ruiz López", "1990-01-01", "611222333", "a@b.es",
                "6", "PAC-3", "s");
        assertTrue(salida.contains("Alta: PAC-3 · Ana Ruiz"));
        assertTrue(salida.contains("Modificado: PAC-3 · Ana Ruiz López"));
        assertTrue(salida.contains("Paciente dado de baja"));
        assertTrue(salida.contains("PAC-1 · Lucía Martín Pérez"));
    }

    @Test
    @DisplayName("Un error de validación se muestra y el menú sigue")
    void errorValidacion() {
        String salida = admision("2", "11111111A", "Ana", "Ruiz", "1990-01-01", "611222333", "");
        assertTrue(salida.contains("ERROR: La letra del DNI no es correcta"));
    }

    @Test
    @DisplayName("CU05: crear agenda, ver agendas del día y huecos libres (con modalidad mal escrita)")
    void agendas() {
        String salida = admision(
                "7", "2026-11-20", "RM1", "xx", "rm", "medico1", "16:00", "17:00", "30",
                "8", "2026-11-20",
                "9", "RM", "2026-11-20", "2026-11-20");
        assertTrue(salida.contains("Modalidad desconocida: XX"));
        assertTrue(salida.contains("Creada: Agenda"));
        assertTrue(salida.contains("2026-11-20 16:30-17:00 · sala RM1"));
    }

    @Test
    @DisplayName("CU06: pendientes, asignar, reprogramar, ver del día y cancelar")
    void citas() {
        String salida = admision(
                "10",
                "11", "2", "2", "12:30",
                "12", "2", "2", "13:00",
                "14", MANANA.toString(),
                "13", "2", "Paciente ingresado",
                "6", "PAC-9", "n");
        assertTrue(salida.contains("Solicitud 2 · TC-CRANEO"));
        assertTrue(salida.contains("Asignada: Cita 2"));
        assertTrue(salida.contains("Reprogramada: Cita 2"));
        assertTrue(salida.contains("Cita cancelada"));
    }
}
