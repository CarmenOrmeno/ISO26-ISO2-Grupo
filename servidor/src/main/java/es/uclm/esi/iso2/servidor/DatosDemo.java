package es.uclm.esi.iso2.servidor;

import java.time.LocalDate;

import es.uclm.esi.iso2.servidor.catalogo.Modalidad;
import es.uclm.esi.iso2.servidor.catalogo.Prioridad;
import es.uclm.esi.iso2.servidor.catalogo.ServicioCatalogo;
import es.uclm.esi.iso2.servidor.catalogo.TipoPrueba;
import es.uclm.esi.iso2.servidor.pacientes.DatosPaciente;
import es.uclm.esi.iso2.servidor.seguridad.Rol;
import es.uclm.esi.iso2.servidor.solicitudes.DatosSolicitud;

/** Datos de demostración para probar el cliente sin sistemas reales. */
final class DatosDemo {

    static final String CONTRASENA = "demo";

    private DatosDemo() {
    }

    static void cargar(Servidor s) {
        // CU01
        s.registrarUsuario("paciente1", CONTRASENA, "Lucía Martín Pérez", Rol.PACIENTE, "PAC-1");
        s.registrarUsuario("medico1", CONTRASENA, "Dr. Javier Ruiz Gómez", Rol.MEDICO, null);
        s.registrarUsuario("admision1", CONTRASENA, "Marta López Sanz", Rol.ADMINISTRATIVO, null);
        s.registrarUsuario("admin1", CONTRASENA, "Pedro García Díaz", Rol.ADMINISTRADOR, null);

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
    }
}
