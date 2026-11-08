package es.uclm.esi.iso2.servidor.controlador;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import es.uclm.esi.iso2.servidor.Servidor;
import es.uclm.esi.iso2.servidor.agendas.DatosAgenda;
import es.uclm.esi.iso2.servidor.catalogo.Modalidad;
import es.uclm.esi.iso2.servidor.catalogo.Prioridad;
import es.uclm.esi.iso2.servidor.catalogo.TipoPrueba;
import es.uclm.esi.iso2.servidor.comun.RelojAjustable;
import es.uclm.esi.iso2.servidor.pacientes.DatosPaciente;
import es.uclm.esi.iso2.servidor.seguridad.Rol;
import es.uclm.esi.iso2.servidor.solicitudes.DatosSolicitud;

/** Servidor completo para las pruebas de integración de los controladores. Lunes 02/11/2026 9:00. */
class Entorno {

    static final String CLAVE = "x";
    static final LocalDateTime INICIO = LocalDateTime.of(2026, 11, 2, 9, 0);

    final RelojAjustable reloj = new RelojAjustable(INICIO);
    final Servidor servidor = new Servidor(reloj);

    Entorno() {
        servidor.registrarUsuario("paciente1", CLAVE, "Lucía Martín", Rol.PACIENTE, "PAC-1");
        servidor.registrarUsuario("paciente2", CLAVE, "Antonio Gil", Rol.PACIENTE, "PAC-2");
        servidor.registrarUsuario("medico1", CLAVE, "Dr. Ruiz", Rol.MEDICO, null);
        servidor.registrarUsuario("medico2", CLAVE, "Dra. Sanz", Rol.MEDICO, null);
        servidor.registrarUsuario("admision1", CLAVE, "Marta López", Rol.ADMINISTRATIVO, null);
        servidor.registrarUsuario("admin1", CLAVE, "Pedro García", Rol.ADMINISTRADOR, null);
    }

    String token(String login) {
        return servidor.sesion().iniciarSesion(login, CLAVE);
    }

    /** Pacientes PAC-1 y PAC-2 (asociados a paciente1 y paciente2) y un catálogo con RX-T, US-A y TC-C. */
    Entorno conDatosBasicos() {
        String t = token("admision1");
        servidor.pacientes().alta(t, new DatosPaciente("12345678Z", "Lucía", "Martín",
                LocalDate.of(1980, 5, 14), "600111222", null));
        servidor.pacientes().alta(t, new DatosPaciente("87654321X", "Antonio", "Gil",
                LocalDate.of(1955, 9, 3), "925000111", null));
        String a = token("admin1");
        servidor.catalogo().alta(a, new TipoPrueba("RX-T", "Rx tórax", Modalidad.RX, "RX1", 10, "", false, false, "", false));
        servidor.catalogo().alta(a, new TipoPrueba("US-A", "Eco abdominal", Modalidad.US, "ECO1", 20, "Ayuno de 6 h",
                false, false, "", true));
        servidor.catalogo().alta(a, new TipoPrueba("TC-C", "TAC craneal", Modalidad.TC, "TC1", 30, "", true, true, "", true));
        return this;
    }

    long solicitar(String medico, String paciente, String tipo, Prioridad prioridad) {
        return servidor.solicitudes().solicitar(token(medico),
                new DatosSolicitud(paciente, tipo, "Indicación", "Justificación", prioridad)).getId();
    }

    static final LocalDate MANANA = INICIO.toLocalDate().plusDays(1);

    /** Agenda de mañana de 09:00 a 10:00 con huecos de 15 min. Devuelve su id. */
    long agenda(Modalidad modalidad, String sala) {
        return agenda(modalidad, sala, "medico1");
    }

    long agenda(Modalidad modalidad, String sala, String medico) {
        return servidor.agendas().crear(token("admision1"), new DatosAgenda(MANANA, sala, modalidad, medico,
                LocalTime.of(9, 0), LocalTime.of(10, 0), 15)).getId();
    }

    long citar(long idSolicitud, long idAgenda, String hora) {
        return servidor.citas().asignar(token("admision1"), idSolicitud, idAgenda, LocalTime.parse(hora)).getId();
    }
}
