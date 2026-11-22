package es.uclm.esi.iso2.servidor;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Las entidades que devuelve la API no se pueden modificar desde fuera de su paquete: sus métodos de cambio
 * de estado son de paquete y solo los usa su servicio, que comprueba las reglas de negocio. Así el cliente no
 * puede saltarse el control de acceso, la auditoría ni las reglas cambiando un objeto devuelto.
 */
class EncapsulacionTest {

    @ParameterizedTest(name = "{0} no tiene métodos públicos de modificación")
    @ValueSource(strings = { "citas.Cita", "agendas.Agenda", "agendas.Hueco", "pacientes.Paciente",
            "solicitudes.SolicitudPrueba", "seguimiento.Seguimiento", "notificaciones.Notificacion",
            "atencion.IncidenciaPaciente", "seguridad.Sesion", "seguridad.PoliticaPermisos",
            "catalogo.ParametrosSistema" })
    void sinModificadoresPublicos(String clase) throws ClassNotFoundException {
        Class<?> tipo = Class.forName("es.uclm.esi.iso2.servidor." + clase);
        List<String> modificadores = Arrays.stream(tipo.getDeclaredMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()) && !Modifier.isStatic(m.getModifiers()))
                .filter(m -> m.getReturnType() == void.class)
                .map(Method::getName).toList();
        assertTrue(modificadores.isEmpty(), clase + " expone " + modificadores);
    }
}
