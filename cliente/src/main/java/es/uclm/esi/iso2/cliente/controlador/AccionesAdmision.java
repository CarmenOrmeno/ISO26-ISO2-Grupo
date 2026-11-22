package es.uclm.esi.iso2.cliente.controlador;

import static es.uclm.esi.iso2.cliente.controlador.Accion.de;
import static es.uclm.esi.iso2.cliente.controlador.Accion.orden;
import static es.uclm.esi.iso2.cliente.controlador.Campo.entero;
import static es.uclm.esi.iso2.cliente.controlador.Campo.fecha;
import static es.uclm.esi.iso2.cliente.controlador.Campo.hora;
import static es.uclm.esi.iso2.cliente.controlador.Campo.id;
import static es.uclm.esi.iso2.cliente.controlador.Campo.opcion;
import static es.uclm.esi.iso2.cliente.controlador.Campo.texto;
import static es.uclm.esi.iso2.cliente.controlador.Campo.textoOpcional;

import java.util.List;

import es.uclm.esi.iso2.servidor.agendas.DatosAgenda;
import es.uclm.esi.iso2.servidor.api.ApiAgendas;
import es.uclm.esi.iso2.servidor.api.ApiAtencion;
import es.uclm.esi.iso2.servidor.api.ApiCitas;
import es.uclm.esi.iso2.servidor.api.ApiEsperas;
import es.uclm.esi.iso2.servidor.api.ApiInformesOperativos;
import es.uclm.esi.iso2.servidor.api.ApiPacientes;
import es.uclm.esi.iso2.servidor.catalogo.Modalidad;
import es.uclm.esi.iso2.servidor.catalogo.Prioridad;
import es.uclm.esi.iso2.servidor.pacientes.DatosPaciente;

/** Acciones del personal de admisión (CU02, CU05, CU06, CU11, CU15, CU21). */
final class AccionesAdmision {

    private AccionesAdmision() {
    }

    private static final Campo[] DATOS_PACIENTE = { texto("DNI"), texto("Nombre"), texto("Apellidos"),
            fecha("Fecha de nacimiento"), texto("Teléfono"), textoOpcional("Correo") };

    private static DatosPaciente paciente(Valores v, int desde) {
        return new DatosPaciente(v.texto(desde), v.texto(desde + 1), v.texto(desde + 2), v.fecha(desde + 3),
                v.texto(desde + 4), v.texto(desde + 5));
    }

    private static Campo[] con(Campo primero, Campo[] resto) {
        Campo[] todos = new Campo[resto.length + 1];
        todos[0] = primero;
        System.arraycopy(resto, 0, todos, 1, resto.length);
        return todos;
    }

    static List<Accion> crear(ApiPacientes pacientes, ApiAgendas agendas, ApiCitas citas, ApiEsperas esperas,
            ApiAtencion atencion, ApiInformesOperativos informes) {
        return List.of(
                // CU02
                de("CU02 Pacientes", "Pacientes activos", (t, v) -> pacientes.listarActivos(t)),
                de("CU02 Pacientes", "Buscar por DNI", (t, v) -> pacientes.buscarPorDni(t, v.texto(0)), texto("DNI")),
                de("CU02 Pacientes", "Ver paciente", (t, v) -> pacientes.obtener(t, v.texto(0)), texto("Id del paciente")),
                de("CU02 Pacientes", "Alta de paciente", (t, v) -> pacientes.alta(t, paciente(v, 0)), DATOS_PACIENTE),
                de("CU02 Pacientes", "Modificar paciente", (t, v) -> pacientes.modificar(t, v.texto(0), paciente(v, 1)),
                        con(texto("Id del paciente"), DATOS_PACIENTE)),
                orden("CU02 Pacientes", "Baja de paciente", "Paciente dado de baja",
                        (t, v) -> pacientes.baja(t, v.texto(0)), texto("Id del paciente")),
                // CU05
                de("CU05 Agendas", "Agendas de un día", (t, v) -> agendas.delDia(t, v.fecha(0)), fecha("Fecha")),
                de("CU05 Agendas", "Crear agenda", (t, v) -> agendas.crear(t, new DatosAgenda(v.fecha(0), v.texto(1),
                        v.opcion(2, Modalidad.class), v.texto(3), v.hora(4), v.hora(5), v.entero(6))),
                        fecha("Fecha"), texto("Sala"), opcion("Modalidad", Modalidad.values()),
                        texto("Médico (login)"), hora("Inicio"), hora("Fin"), entero("Minutos por hueco")),
                orden("CU05 Agendas", "Eliminar agenda", "Agenda eliminada", (t, v) -> agendas.eliminar(t, v.largo(0)),
                        id("Id de la agenda")),
                de("CU05 Agendas", "Huecos libres", (t, v) -> agendas.huecosLibres(t, v.opcion(0, Modalidad.class),
                        v.fecha(1), v.fecha(2)), opcion("Modalidad", Modalidad.values()), fecha("Desde"), fecha("Hasta")),
                // CU06
                de("CU06 Citas", "Solicitudes pendientes de cita", (t, v) -> citas.solicitudesPendientes(t)),
                de("CU06 Citas", "Asignar cita", (t, v) -> citas.asignar(t, v.largo(0), v.largo(1), v.hora(2)),
                        id("Id de la solicitud"), id("Id de la agenda"), hora("Hora")),
                de("CU06 Citas", "Reprogramar cita", (t, v) -> citas.reprogramar(t, v.largo(0), v.largo(1), v.hora(2)),
                        id("Id de la cita"), id("Id de la nueva agenda"), hora("Hora")),
                orden("CU06 Citas", "Cancelar cita", "Cita cancelada; la solicitud vuelve a la lista de espera",
                        (t, v) -> citas.cancelar(t, v.largo(0), v.texto(1)), id("Id de la cita"), texto("Motivo")),
                de("CU06 Citas", "Citas de un día", (t, v) -> citas.delDia(t, v.fecha(0)), fecha("Fecha")),
                de("CU06 Citas", "Citas de un paciente", (t, v) -> citas.dePaciente(t, v.texto(0)),
                        texto("Id del paciente")),
                de("CU06 Citas", "Cambios solicitados", (t, v) -> citas.conCambioSolicitado(t)),
                // CU11
                de("CU11 Listas de espera", "Lista de espera", (t, v) -> esperas.listaDeEspera(t)),
                de("CU11 Listas de espera", "Lista de espera de una modalidad",
                        (t, v) -> esperas.listaDeEspera(t, v.opcion(0, Modalidad.class)),
                        opcion("Modalidad", Modalidad.values())),
                orden("CU11 Listas de espera", "Cambiar prioridad", "Prioridad cambiada",
                        (t, v) -> esperas.cambiarPrioridad(t, v.largo(0), v.opcion(1, Prioridad.class)),
                        id("Id de la solicitud"), opcion("Prioridad", Prioridad.values())),
                de("CU11 Listas de espera", "Citar en el primer hueco", (t, v) -> esperas.citarEnPrimerHueco(t,
                        v.largo(0)), id("Id de la solicitud")),
                de("CU11 Listas de espera", "Rellenar huecos de una modalidad", (t, v) -> "Citas asignadas: "
                        + esperas.rellenarHuecos(t, v.opcion(0, Modalidad.class)), opcion("Modalidad", Modalidad.values())),
                // CU15
                de("CU15 Atención al paciente", "Incidencias abiertas", (t, v) -> atencion.incidenciasAbiertas(t)),
                de("CU15 Atención al paciente", "Responder incidencia", (t, v) -> atencion.responderIncidencia(t,
                        v.largo(0), v.texto(1)), id("Id de la incidencia"), texto("Respuesta")),
                de("CU15 Atención al paciente", "Encuestas respondidas", (t, v) -> atencion.encuestas(t)),
                // CU21
                de("CU21 Informes operativos", "Informe operativo", (t, v) -> informes.generar(t,
                        v.fecha(0), v.fecha(1)), fecha("Desde"), fecha("Hasta")));
    }
}
