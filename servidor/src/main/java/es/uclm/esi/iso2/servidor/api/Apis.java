package es.uclm.esi.iso2.servidor.api;

/**
 * Punto de acceso del cliente a los contratos del servidor (D-007). No añade operaciones: cada área sigue
 * teniendo su propia interfaz {@code ApiXxx} (ISP). El cliente depende de esta interfaz y no de la clase
 * {@code Servidor}, que la implementa.
 */
public interface Apis {

    ApiSesion sesion();

    ApiPacientes pacientes();

    ApiCatalogo catalogo();

    ApiSolicitudes solicitudes();

    ApiAgendas agendas();

    ApiCitas citas();

    ApiMisCitas misCitas();

    ApiNotificaciones notificaciones();

    ApiRealizacion realizacion();

    ApiInformes informes();

    ApiSeguimiento seguimiento();

    ApiMisSeguimientos misSeguimientos();

    ApiEsperas esperas();

    ApiEstadoSolicitudes estadoSolicitudes();

    ApiHistorial historial();

    ApiAtencionPaciente atencionPaciente();

    ApiAtencion atencion();

    ApiPermisos permisos();

    ApiAuditoria auditoria();

    ApiIntegracion integracion();

    ApiEstadisticasMedico estadisticasMedico();

    ApiInformesOperativos informesOperativos();

    ApiEstadisticasGlobales estadisticasGlobales();
}
