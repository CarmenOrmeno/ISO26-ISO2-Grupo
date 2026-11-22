package es.uclm.esi.iso2.servidor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import es.uclm.esi.iso2.servidor.agendas.DatosAgenda;
import es.uclm.esi.iso2.servidor.catalogo.Modalidad;
import es.uclm.esi.iso2.servidor.catalogo.Prioridad;
import es.uclm.esi.iso2.servidor.catalogo.ServicioCatalogo;
import es.uclm.esi.iso2.servidor.catalogo.TipoPrueba;
import es.uclm.esi.iso2.servidor.pacientes.DatosPaciente;
import es.uclm.esi.iso2.servidor.seguimiento.DatosSeguimiento;
import es.uclm.esi.iso2.servidor.seguridad.Rol;
import es.uclm.esi.iso2.servidor.solicitudes.DatosSolicitud;

/** Datos de demostración para probar el cliente sin sistemas reales. */
final class DatosDemo {

    static final String CONTRASENA = "demo";

    private DatosDemo() {
    }

    /** Usuario de demostración: cuenta del SSO simulado y usuario de la aplicación. */
    private record CuentaDemo(String login, String nombre, Rol rol, String idPaciente) {
    }

    private static final List<CuentaDemo> CUENTAS = List.of(
            new CuentaDemo("paciente1", "Lucía Martín Pérez", Rol.PACIENTE, "PAC-1"),
            new CuentaDemo("medico1", "Dr. Javier Ruiz Gómez", Rol.MEDICO, null),
            new CuentaDemo("admision1", "Marta López Sanz", Rol.ADMINISTRATIVO, null),
            new CuentaDemo("admin1", "Pedro García Díaz", Rol.ADMINISTRADOR, null));

    /**
     * Las cuentas del SSO simulado (sistema externo, en memoria) se registran en cada arranque. Los datos de
     * la aplicación solo se cargan si el almacén está vacío, para no duplicarlos al reiniciar con MySQL.
     */
    static void cargar(Servidor s) {
        boolean primerArranque = s.sinDatos();
        for (CuentaDemo c : CUENTAS) {
            if (primerArranque) {
                s.registrarUsuario(c.login(), CONTRASENA, c.nombre(), c.rol(), c.idPaciente());
            } else {
                s.registrarCuentaSso(c.login(), CONTRASENA, c.nombre());
            }
        }
        if (primerArranque) {
            cargarDatosClinicos(s);
        }
    }

    private static void cargarDatosClinicos(Servidor s) {

        // CU02: el primer paciente recibe el id PAC-1, asociado al usuario paciente1
        s.servicioPacientes().alta(new DatosPaciente("12345678Z", "Lucía", "Martín Pérez",
                LocalDate.of(1980, 5, 14), "600111222", "lucia@example.com"));
        s.servicioPacientes().alta(new DatosPaciente("87654321X", "Antonio", "Gil Moreno",
                LocalDate.of(1955, 9, 3), "925000111", null));

        // CU03: catálogo inicial (ejemplos del enunciado)
        ServicioCatalogo c = s.servicioCatalogo();
        c.alta(new TipoPrueba("RX-TORAX", "Radiografía de tórax", Modalidad.RX, "RX1", 10, "", false, false,
                "Retirar objetos metálicos", false));
        c.alta(new TipoPrueba("US-ABD", "Ecografía abdominal", Modalidad.US, "ECO1", 20,
                "Ayuno de 6 horas", false, false, "", true));
        c.alta(new TipoPrueba("MG-CRIBADO", "Mamografía de cribado", Modalidad.MG, "MAMO1", 15, "", false, false,
                "", true));
        c.alta(new TipoPrueba("TC-CRANEO", "TAC craneal con contraste", Modalidad.TC, "TC1", 30,
                "Ayuno de 4 horas; análisis de función renal", true, true, "Alergias a contrastes yodados", true));
        c.alta(new TipoPrueba("RM-RODILLA", "Resonancia de rodilla", Modalidad.RM, "RM1", 40, "", false, true,
                "Sin marcapasos ni implantes metálicos", false));

        // CU04: solicitudes del médico de demostración
        s.servicioSolicitudes().solicitar("medico1", new DatosSolicitud("PAC-1", "RX-TORAX",
                "Tos persistente de 3 semanas", "Descartar neumonía", Prioridad.ORDINARIO));
        s.servicioSolicitudes().solicitar("medico1", new DatosSolicitud("PAC-2", "TC-CRANEO",
                "Cefalea con signos de alarma", "Descartar lesión ocupante de espacio", Prioridad.PREFERENTE));

        // CU10: seguimiento semestral de ecografía abdominal para PAC-1 (3 revisiones, la primera en un mes)
        s.servicioSeguimiento().prescribir("medico1", new DatosSeguimiento("PAC-1", "US-ABD",
                "Control de esteatosis hepática", "Seguimiento evolutivo", Prioridad.ORDINARIO, 6, 3,
                LocalDate.now(s.reloj()).plusMonths(1)));

        // CU05: agendas de los próximos 5 días laborables a partir de mañana
        LocalDate dia = LocalDate.now(s.reloj()).plusDays(1);
        for (int creadas = 0; creadas < 5; dia = dia.plusDays(1)) {
            if (dia.getDayOfWeek().getValue() <= 5) {
                s.servicioAgendas().crear(new DatosAgenda(dia, "RX1", Modalidad.RX, "medico1",
                        LocalTime.of(9, 0), LocalTime.of(11, 0), 15));
                s.servicioAgendas().crear(new DatosAgenda(dia, "TC1", Modalidad.TC, "medico1",
                        LocalTime.of(12, 0), LocalTime.of(14, 0), 30));
                creadas++;
            }
        }

        // CU06: la primera solicitud (Rx de tórax de PAC-1) se cita en el primer hueco libre de RX
        var hueco = s.servicioAgendas().huecosLibres(Modalidad.RX, dia.minusDays(14), dia).get(0);
        s.servicioCitas().asignar(1, hueco.idAgenda(), hueco.inicio());
    }
}
