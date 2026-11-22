package es.uclm.esi.iso2.cliente.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Sistema: las acciones del cliente contra el servidor de demostración. */
class CatalogoAccionesTest {

    /** Un valor con el formato correcto para cada tipo de campo. */
    private static String ejemplo(Campo c) {
        return switch (c.tipo()) {
            case TEXTO -> "x";
            case TEXTO_OPCIONAL, LARGO_OPCIONAL -> "";
            case FECHA -> LocalDate.of(2026, 11, 2).toString();
            case HORA -> "09:00";
            case ENTERO, LARGO -> "1";
            case OPCION, SI_NO -> c.opciones().get(0);
        };
    }

    @ParameterizedTest(name = "todas las acciones de {0} están permitidas a su rol")
    @ValueSource(strings = { "paciente1", "medico1", "admision1", "admin1" })
    @DisplayName("Coherencia cliente-servidor: ninguna acción del menú de un rol choca con la política de permisos")
    void accionesPermitidas(String login) {
        SesionCliente s = Demo.sesion(login);
        for (Accion a : s.acciones()) {
            Resultado r = s.ejecutar(a, a.campos().stream().map(CatalogoAccionesTest::ejemplo).toList());
            assertFalse(r.mensaje().contains("no tiene el permiso"), a.nombre() + ": " + r.mensaje());
            assertFalse(r.mensaje().contains("Sesión no válida"), a.nombre() + ": " + r.mensaje());
        }
    }

    @Test
    @DisplayName("Paciente: ve su cita de demostración")
    void paciente() {
        SesionCliente s = Demo.sesion("paciente1");
        Resultado r = s.ejecutar(Demo.accion(s, "Ver mis citas"), List.of());
        assertEquals("1 resultado", r.mensaje());
    }

    @Test
    @DisplayName("Médico: sus dos solicitudes de demostración; sin paciente asociado, su historial da error")
    void medico() {
        SesionCliente s = Demo.sesion("medico1");
        assertEquals("2 resultados", s.ejecutar(Demo.accion(s, "Mis solicitudes"), List.of()).mensaje());
        Resultado historial = s.ejecutar(Demo.accion(s, "Mi historial"), List.of());
        assertFalse(historial.correcto());
        assertTrue(historial.mensaje().contains("no tiene un paciente asociado"));
    }

    @Test
    @DisplayName("Admisión: busca un paciente por DNI; una fecha mal escrita da error sin llamar al servidor")
    void admision() {
        SesionCliente s = Demo.sesion("admision1");
        Resultado r = s.ejecutar(Demo.accion(s, "Buscar por DNI"), List.of("12345678Z"));
        assertTrue(r.correcto());
        assertTrue(String.join("\n", r.lineas()).contains("Lucía"));
        Resultado mal = s.ejecutar(Demo.accion(s, "Agendas de un día"), List.of("mañana"));
        assertFalse(mal.correcto());
        assertTrue(mal.mensaje().contains("AAAA-MM-DD"));
    }

    @Test
    @DisplayName("Administrador: estado de los 4 sistemas e importación del paciente nuevo de admisión (CU19)")
    void administrador() {
        SesionCliente s = Demo.sesion("admin1");
        assertEquals("4 resultados", s.ejecutar(Demo.accion(s, "Estado de los sistemas"), List.of()).mensaje());
        Resultado importacion = s.ejecutar(Demo.accion(s, "Importar pacientes de admisión"), List.of());
        assertTrue(String.join("\n", importacion.lineas()).contains("1 procesados"));
        Resultado alta = s.ejecutar(Demo.accion(s, "Alta de tipo de prueba"), List.of("US-T", "Eco tiroidea", "US",
                "ECO1", "20", "", "n", "n", "", "s"));
        assertTrue(alta.correcto(), alta.mensaje());
    }
}
