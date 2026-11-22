package es.uclm.esi.iso2.cliente.controlador;

import static es.uclm.esi.iso2.cliente.controlador.Accion.de;
import static es.uclm.esi.iso2.cliente.controlador.Accion.orden;
import static es.uclm.esi.iso2.cliente.controlador.Campo.entero;
import static es.uclm.esi.iso2.cliente.controlador.Campo.fecha;
import static es.uclm.esi.iso2.cliente.controlador.Campo.id;
import static es.uclm.esi.iso2.cliente.controlador.Campo.opcion;
import static es.uclm.esi.iso2.cliente.controlador.Campo.texto;
import static es.uclm.esi.iso2.cliente.controlador.Campo.textoOpcional;

import java.util.List;

import es.uclm.esi.iso2.servidor.Servidor;
import es.uclm.esi.iso2.servidor.catalogo.Modalidad;
import es.uclm.esi.iso2.servidor.catalogo.Prioridad;
import es.uclm.esi.iso2.servidor.realizacion.TipoIncidencia;
import es.uclm.esi.iso2.servidor.seguimiento.DatosSeguimiento;
import es.uclm.esi.iso2.servidor.solicitudes.DatosSolicitud;
import es.uclm.esi.iso2.servidor.solicitudes.EstadoSolicitud;

/** Acciones propias del médico (CU04, CU08, CU09, CU10, CU14, CU20). */
final class AccionesMedico {

    private AccionesMedico() {
    }

    static List<Accion> crear(Servidor s) {
        var solicitudes = s.solicitudes();
        var estado = s.estadoSolicitudes();
        var realizacion = s.realizacion();
        var informes = s.informes();
        var seguimiento = s.seguimiento();
        return List.of(
                // CU04
                de("CU04 Solicitar pruebas", "Tipos de prueba", (t, v) -> s.catalogo().listar(t)),
                de("CU04 Solicitar pruebas", "Solicitar prueba", (t, v) -> solicitudes.solicitar(t, new DatosSolicitud(
                        v.texto(0), v.texto(1), v.texto(2), v.texto(3), v.opcion(4, Prioridad.class))),
                        texto("Id del paciente"), texto("Código del tipo de prueba"), texto("Indicación clínica"),
                        texto("Justificación"), opcion("Prioridad", Prioridad.values())),
                orden("CU04 Solicitar pruebas", "Cancelar solicitud", "Solicitud cancelada",
                        (t, v) -> solicitudes.cancelar(t, v.largo(0), v.texto(1)), id("Id de la solicitud"),
                        texto("Motivo")),
                // CU14
                de("CU14 Estado y agendas", "Mis solicitudes", (t, v) -> estado.misSolicitudes(t)),
                de("CU14 Estado y agendas", "Mis solicitudes por estado",
                        (t, v) -> estado.misSolicitudes(t, v.opcion(0, EstadoSolicitud.class)),
                        opcion("Estado", EstadoSolicitud.values())),
                de("CU14 Estado y agendas", "Situación de una solicitud", (t, v) -> estado.situacion(t, v.largo(0)),
                        id("Id de la solicitud")),
                de("CU14 Estado y agendas", "Mis agendas", (t, v) -> estado.misAgendas(t, v.fecha(0), v.fecha(1)),
                        fecha("Desde"), fecha("Hasta")),
                de("CU14 Estado y agendas", "Huecos libres", (t, v) -> estado.huecosLibres(t,
                        v.opcion(0, Modalidad.class), v.fecha(1), v.fecha(2)), opcion("Modalidad", Modalidad.values()),
                        fecha("Desde"), fecha("Hasta")),
                // CU08
                de("CU08 Realización", "Registrar realización", (t, v) -> realizacion.registrarRealizacion(t,
                        v.largo(0), v.entero(1), v.textoOVacio(2)), id("Id de la cita"), entero("Número de imágenes"),
                        textoOpcional("Observaciones")),
                de("CU08 Realización", "Registrar incidencia", (t, v) -> realizacion.registrarIncidencia(t,
                        v.largo(0), v.opcion(1, TipoIncidencia.class), v.texto(2)), id("Id de la cita"),
                        opcion("Tipo", TipoIncidencia.values()), texto("Descripción")),
                de("CU08 Realización", "Incidencias de una cita", (t, v) -> realizacion.incidenciasDeCita(t, v.largo(0)),
                        id("Id de la cita")),
                // CU09
                de("CU09 Informes e imágenes", "Crear informe", (t, v) -> informes.crearInforme(t, v.largo(0),
                        v.texto(1), v.texto(2)), id("Id de la solicitud"), texto("Hallazgos"), texto("Conclusión")),
                de("CU09 Informes e imágenes", "Informe de una solicitud",
                        (t, v) -> informes.informeDeSolicitud(t, v.largo(0)), id("Id de la solicitud")),
                de("CU09 Informes e imágenes", "Informes de un paciente",
                        (t, v) -> informes.informesDePaciente(t, v.texto(0)), texto("Id del paciente")),
                de("CU09 Informes e imágenes", "Imágenes de una prueba", (t, v) -> informes.imagenes(t, v.largo(0)),
                        id("Id de la solicitud")),
                // CU10
                de("CU10 Seguimientos", "Prescribir seguimiento", (t, v) -> seguimiento.prescribir(t,
                        new DatosSeguimiento(v.texto(0), v.texto(1), v.texto(2), v.texto(3),
                                v.opcion(4, Prioridad.class), v.entero(5), v.entero(6), v.fecha(7))),
                        texto("Id del paciente"), texto("Código del tipo de prueba"), texto("Indicación clínica"),
                        texto("Justificación"), opcion("Prioridad", Prioridad.values()),
                        entero("Periodicidad en meses"), entero("Número de revisiones"), fecha("Primera revisión")),
                orden("CU10 Seguimientos", "Cancelar seguimiento", "Seguimiento cancelado",
                        (t, v) -> seguimiento.cancelar(t, v.largo(0), v.texto(1)), id("Id del seguimiento"),
                        texto("Motivo")),
                de("CU10 Seguimientos", "Mis prescripciones", (t, v) -> seguimiento.misPrescripciones(t)),
                // CU20
                de("CU20 Estadísticas", "Mis estadísticas", (t, v) -> s.estadisticasMedico().misEstadisticas(t,
                        v.fecha(0), v.fecha(1)), fecha("Desde"), fecha("Hasta")));
    }
}
