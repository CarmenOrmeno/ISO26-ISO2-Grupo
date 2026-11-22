package es.uclm.esi.iso2.cliente.controlador;

import static es.uclm.esi.iso2.cliente.controlador.Accion.de;
import static es.uclm.esi.iso2.cliente.controlador.Accion.orden;
import static es.uclm.esi.iso2.cliente.controlador.Campo.entero;
import static es.uclm.esi.iso2.cliente.controlador.Campo.fecha;
import static es.uclm.esi.iso2.cliente.controlador.Campo.hora;
import static es.uclm.esi.iso2.cliente.controlador.Campo.opcion;
import static es.uclm.esi.iso2.cliente.controlador.Campo.siNo;
import static es.uclm.esi.iso2.cliente.controlador.Campo.texto;
import static es.uclm.esi.iso2.cliente.controlador.Campo.textoOpcional;

import java.util.List;

import es.uclm.esi.iso2.servidor.api.ApiAuditoria;
import es.uclm.esi.iso2.servidor.api.ApiCatalogo;
import es.uclm.esi.iso2.servidor.api.ApiEstadisticasGlobales;
import es.uclm.esi.iso2.servidor.api.ApiIntegracion;
import es.uclm.esi.iso2.servidor.api.ApiNotificaciones;
import es.uclm.esi.iso2.servidor.api.ApiPermisos;
import es.uclm.esi.iso2.servidor.api.ApiSeguimiento;
import es.uclm.esi.iso2.servidor.catalogo.Modalidad;
import es.uclm.esi.iso2.servidor.catalogo.Prioridad;
import es.uclm.esi.iso2.servidor.catalogo.TipoPrueba;
import es.uclm.esi.iso2.servidor.seguridad.Permiso;
import es.uclm.esi.iso2.servidor.seguridad.Rol;

/** Acciones del administrador (CU03, tareas programadas, CU17, CU18, CU19, CU22). */
final class AccionesAdministrador {

    private AccionesAdministrador() {
    }

    private static final Campo[] TIPO_PRUEBA = { texto("Código"), texto("Nombre"),
            opcion("Modalidad", Modalidad.values()), texto("Sala"), entero("Duración en minutos"),
            textoOpcional("Preparación"), siNo("Contraste"), siNo("Consentimiento"),
            textoOpcional("Requisitos de seguridad"), siNo("Admite seguimiento") };

    private static TipoPrueba tipo(Valores v) {
        return new TipoPrueba(v.texto(0), v.texto(1), v.opcion(2, Modalidad.class), v.texto(3), v.entero(4),
                v.textoOVacio(5), v.siNo(6), v.siNo(7), v.textoOVacio(8), v.siNo(9));
    }

    static List<Accion> crear(ApiCatalogo catalogo, ApiNotificaciones notificaciones, ApiSeguimiento seguimiento,
            ApiPermisos permisos, ApiAuditoria auditoria, ApiIntegracion integracion,
            ApiEstadisticasGlobales estadisticas) {
        return List.of(
                // CU03
                de("CU03 Catálogo y parámetros", "Tipos de prueba", (t, v) -> catalogo.listar(t)),
                de("CU03 Catálogo y parámetros", "Alta de tipo de prueba", (t, v) -> catalogo.alta(t, tipo(v)),
                        TIPO_PRUEBA),
                de("CU03 Catálogo y parámetros", "Modificar tipo de prueba", (t, v) -> catalogo.modificar(t, tipo(v)),
                        TIPO_PRUEBA),
                orden("CU03 Catálogo y parámetros", "Baja de tipo de prueba", "Tipo de prueba dado de baja",
                        (t, v) -> catalogo.baja(t, v.texto(0)), texto("Código")),
                de("CU03 Catálogo y parámetros", "Parámetros del sistema", (t, v) -> catalogo.parametros(t)),
                orden("CU03 Catálogo y parámetros", "Cambiar horario", "Horario cambiado",
                        (t, v) -> catalogo.cambiarHorario(t, v.hora(0), v.hora(1)), hora("Apertura"), hora("Cierre")),
                orden("CU03 Catálogo y parámetros", "Cambiar plazo de una prioridad", "Plazo cambiado",
                        (t, v) -> catalogo.cambiarPlazo(t, v.opcion(0, Prioridad.class), v.entero(1)),
                        opcion("Prioridad", Prioridad.values()), entero("Días")),
                orden("CU03 Catálogo y parámetros", "Cambiar antelación de recordatorios", "Antelación cambiada",
                        (t, v) -> catalogo.cambiarHorasRecordatorio(t, v.entero(0)), entero("Horas")),
                // tareas programadas (actor Temporizador)
                de("Tareas programadas", "Enviar recordatorios (CU12)",
                        (t, v) -> "Citas recordadas: " + notificaciones.enviarRecordatorios(t)),
                de("Tareas programadas", "Generar revisiones de seguimiento (CU10)",
                        (t, v) -> "Solicitudes generadas: " + seguimiento.generarRevisiones(t)),
                // CU17
                de("CU17 Roles y permisos", "Usuarios", (t, v) -> permisos.usuarios(t)),
                de("CU17 Roles y permisos", "Permisos de un rol",
                        (t, v) -> List.copyOf(permisos.permisosDe(t, v.opcion(0, Rol.class))),
                        opcion("Rol", Rol.values())),
                orden("CU17 Roles y permisos", "Conceder permiso", "Permiso concedido",
                        (t, v) -> permisos.conceder(t, v.opcion(0, Rol.class), v.opcion(1, Permiso.class)),
                        opcion("Rol", Rol.values()), opcion("Permiso", Permiso.values())),
                orden("CU17 Roles y permisos", "Revocar permiso", "Permiso revocado",
                        (t, v) -> permisos.revocar(t, v.opcion(0, Rol.class), v.opcion(1, Permiso.class)),
                        opcion("Rol", Rol.values()), opcion("Permiso", Permiso.values())),
                orden("CU17 Roles y permisos", "Restablecer la política por defecto", "Política restablecida",
                        (t, v) -> permisos.restablecer(t)),
                de("CU17 Roles y permisos", "Cambiar el rol de un usuario", (t, v) -> permisos.cambiarRol(t, v.texto(0),
                        v.opcion(1, Rol.class)), texto("Login"), opcion("Nuevo rol", Rol.values())),
                // CU18
                de("CU18 Auditoría", "Registros entre fechas", (t, v) -> auditoria.registros(t, v.fecha(0), v.fecha(1)),
                        fecha("Desde"), fecha("Hasta")),
                de("CU18 Auditoría", "Registros de un usuario", (t, v) -> auditoria.registrosDeUsuario(t, v.texto(0),
                        v.fecha(1), v.fecha(2)), texto("Login"), fecha("Desde"), fecha("Hasta")),
                de("CU18 Auditoría", "Accesos denegados", (t, v) -> auditoria.accesosDenegados(t, v.fecha(0),
                        v.fecha(1)), fecha("Desde"), fecha("Hasta")),
                de("CU18 Auditoría", "Accesos a un recurso", (t, v) -> auditoria.accesosARecurso(t, v.texto(0),
                        v.fecha(1), v.fecha(2)), texto("Texto del recurso"), fecha("Desde"), fecha("Hasta")),
                // CU19
                de("CU19 Integración", "Estado de los sistemas", (t, v) -> integracion.estado(t)),
                de("CU19 Integración", "Importar pacientes de admisión", (t, v) -> integracion.importarPacientes(t)),
                de("CU19 Integración", "Enviar informes a la HCE", (t, v) -> integracion.enviarInformesHce(t)),
                de("CU19 Integración", "Sincronizaciones", (t, v) -> integracion.sincronizaciones(t)),
                // CU22
                de("CU22 Estadísticas globales", "Estadísticas globales", (t, v) -> estadisticas
                        .estadisticas(t, v.fecha(0), v.fecha(1)), fecha("Desde"), fecha("Hasta")));
    }
}
