package es.uclm.esi.iso2.cliente.controlador;

import static es.uclm.esi.iso2.cliente.controlador.Accion.de;
import static es.uclm.esi.iso2.cliente.controlador.Accion.orden;
import static es.uclm.esi.iso2.cliente.controlador.Campo.fecha;
import static es.uclm.esi.iso2.cliente.controlador.Campo.id;
import static es.uclm.esi.iso2.cliente.controlador.Campo.idOpcional;
import static es.uclm.esi.iso2.cliente.controlador.Campo.texto;
import static es.uclm.esi.iso2.cliente.controlador.Campo.textoOpcional;

import java.util.List;

import es.uclm.esi.iso2.servidor.api.ApiAtencionPaciente;
import es.uclm.esi.iso2.servidor.api.ApiHistorial;
import es.uclm.esi.iso2.servidor.api.ApiMisCitas;
import es.uclm.esi.iso2.servidor.api.ApiMisSeguimientos;
import es.uclm.esi.iso2.servidor.api.ApiNotificaciones;
import es.uclm.esi.iso2.servidor.atencion.CategoriaIncidencia;

/** Acciones del paciente (CU07, CU12, CU13, CU15, CU16). El médico también las tiene (D-005). */
final class AccionesPaciente {

    private AccionesPaciente() {
    }

    static List<Accion> crear(ApiMisCitas citas, ApiNotificaciones avisos, ApiHistorial historial,
            ApiAtencionPaciente atencion, ApiMisSeguimientos seguimientos) {
        return List.of(
                // CU07
                de("CU07 Mis citas", "Ver mis citas", (t, v) -> citas.misCitas(t)),
                de("CU07 Mis citas", "Confirmar cita", (t, v) -> citas.confirmar(t, v.largo(0)), id("Id de la cita")),
                de("CU07 Mis citas", "Pedir cambio de cita", (t, v) -> citas.solicitarCambio(t, v.largo(0), v.texto(1)),
                        id("Id de la cita"), texto("Motivo")),
                orden("CU07 Mis citas", "Cancelar cita", "Cita cancelada",
                        (t, v) -> citas.cancelar(t, v.largo(0), v.texto(1)), id("Id de la cita"), texto("Motivo")),
                // CU12
                de("CU12 Notificaciones", "Mis notificaciones", (t, v) -> avisos.misNotificaciones(t)),
                de("CU12 Notificaciones", "Número de no leídas", (t, v) -> "No leídas: " + avisos.noLeidas(t)),
                orden("CU12 Notificaciones", "Marcar como leída", "Notificación leída",
                        (t, v) -> avisos.marcarLeida(t, v.largo(0)), id("Id de la notificación")),
                // CU13
                de("CU13 Historial", "Mi historial", (t, v) -> historial.miHistorial(t)),
                de("CU13 Historial", "Historial entre fechas", (t, v) -> historial.miHistorial(t, v.fecha(0), v.fecha(1)),
                        fecha("Desde"), fecha("Hasta")),
                de("CU13 Historial", "Ver informe de una prueba", (t, v) -> historial.miInforme(t, v.largo(0)),
                        id("Id de la solicitud")),
                // CU15
                de("CU15 Incidencias y encuestas", "Comunicar incidencia", (t, v) -> atencion.comunicarIncidencia(t,
                        v.opcion(0, CategoriaIncidencia.class), v.texto(1), v.largoOpcional(2)),
                        Campo.opcion("Categoría", CategoriaIncidencia.values()), texto("Descripción"),
                        idOpcional("Id de la prueba")),
                de("CU15 Incidencias y encuestas", "Mis incidencias", (t, v) -> atencion.misIncidencias(t)),
                de("CU15 Incidencias y encuestas", "Encuestas pendientes", (t, v) -> atencion.encuestasPendientes(t)),
                de("CU15 Incidencias y encuestas", "Responder encuesta", (t, v) -> atencion.responderEncuesta(t,
                        v.largo(0), v.entero(1), v.textoOVacio(2)), id("Id de la solicitud"),
                        Campo.entero("Puntuación de 1 a 5"), textoOpcional("Comentario")),
                de("CU15 Incidencias y encuestas", "Mis encuestas", (t, v) -> atencion.misEncuestas(t)),
                // CU16
                de("CU16 Mis seguimientos", "Mis seguimientos", (t, v) -> seguimientos.misSeguimientos(t)),
                de("CU16 Mis seguimientos", "Revisiones de un seguimiento",
                        (t, v) -> seguimientos.revisiones(t, v.largo(0)), id("Id del seguimiento")));
    }
}
