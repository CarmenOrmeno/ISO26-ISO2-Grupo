package es.uclm.esi.iso2.cliente.controlador;

import es.uclm.esi.iso2.cliente.vista.Consola;
import es.uclm.esi.iso2.servidor.agendas.DatosAgenda;
import es.uclm.esi.iso2.servidor.api.ApiAgendas;
import es.uclm.esi.iso2.servidor.api.ApiCitas;
import es.uclm.esi.iso2.servidor.api.ApiPacientes;
import es.uclm.esi.iso2.servidor.api.ApiSesion;
import es.uclm.esi.iso2.servidor.catalogo.Modalidad;
import es.uclm.esi.iso2.servidor.pacientes.DatosPaciente;

/** Controlador del menú del personal de admisión: CU02 pacientes, CU05 agendas y CU06 citas. */
public final class MenuAdmision {

    private MenuAdmision() {
    }

    public static Menu crear(Consola c, ApiSesion sesion, ApiPacientes pacientes, ApiAgendas agendas, ApiCitas citas) {
        Menu menu = new Menu("Menú de admisión", c, sesion);
        // CU02
        menu.agregar("Alta de paciente", t -> c.mostrar("Alta: " + pacientes.alta(t, leerPaciente(c))));
        menu.agregar("Buscar paciente por DNI", t -> c.mostrar(pacientes.buscarPorDni(t, c.leer("DNI")).toString()));
        menu.agregar("Listar pacientes activos", t -> c.listar("Pacientes", pacientes.listarActivos(t)));
        menu.agregar("Modificar paciente", t -> {
            String id = c.leer("Id del paciente");
            c.mostrar("Modificado: " + pacientes.modificar(t, id, leerPaciente(c)));
        });
        menu.agregar("Baja de paciente", t -> {
            String id = c.leer("Id del paciente");
            if (c.confirmar("¿Dar de baja a " + id + "?")) {
                pacientes.baja(t, id);
                c.mostrar("Paciente dado de baja");
            }
        });
        // CU05
        menu.agregar("Crear agenda", t -> c.mostrar("Creada: " + agendas.crear(t, new DatosAgenda(
                c.leerFecha("Fecha"), c.leer("Sala"), leerModalidad(c), c.leer("Médico (login)"),
                c.leerHora("Inicio"), c.leerHora("Fin"), c.leerEntero("Minutos por hueco")))));
        menu.agregar("Agendas de un día", t -> c.listar("Agendas", agendas.delDia(t, c.leerFecha("Fecha"))));
        menu.agregar("Huecos libres", t -> {
            Modalidad m = leerModalidad(c);
            c.listar("Huecos libres", agendas.huecosLibres(t, m, c.leerFecha("Desde"), c.leerFecha("Hasta")));
        });
        // CU06
        menu.agregar("Solicitudes pendientes de cita", t -> c.listar("Pendientes", citas.solicitudesPendientes(t)));
        menu.agregar("Asignar cita", t -> c.mostrar("Asignada: " + citas.asignar(t,
                c.leerLargo("Id de la solicitud"), c.leerLargo("Id de la agenda"), c.leerHora("Hora"))));
        menu.agregar("Reprogramar cita", t -> c.mostrar("Reprogramada: " + citas.reprogramar(t,
                c.leerLargo("Id de la cita"), c.leerLargo("Id de la nueva agenda"), c.leerHora("Hora"))));
        menu.agregar("Cancelar cita", t -> {
            citas.cancelar(t, c.leerLargo("Id de la cita"), c.leer("Motivo"));
            c.mostrar("Cita cancelada; la solicitud vuelve a la lista de espera");
        });
        menu.agregar("Citas de un día", t -> c.listar("Citas", citas.delDia(t, c.leerFecha("Fecha"))));
        return menu;
    }

    static DatosPaciente leerPaciente(Consola c) {
        return new DatosPaciente(c.leer("DNI"), c.leer("Nombre"), c.leer("Apellidos"),
                c.leerFecha("Fecha de nacimiento"), c.leer("Teléfono"), c.leer("Correo (opcional)"));
    }

    static Modalidad leerModalidad(Consola c) {
        while (true) {
            String texto = c.leer("Modalidad (RX, US, MG, TC, RM)").toUpperCase(java.util.Locale.ROOT);
            try {
                return Modalidad.valueOf(texto);
            } catch (IllegalArgumentException e) {
                c.error("Modalidad desconocida: " + texto);
            }
        }
    }
}
