package es.uclm.esi.iso2.servidor;

import java.time.Clock;
import java.util.List;

import es.uclm.esi.iso2.servidor.agendas.Agenda;
import es.uclm.esi.iso2.servidor.agendas.ServicioAgendas;
import es.uclm.esi.iso2.servidor.agendas.ServicioAgendasImpl;
import es.uclm.esi.iso2.servidor.api.ApiAgendas;
import es.uclm.esi.iso2.servidor.api.ApiAtencion;
import es.uclm.esi.iso2.servidor.api.ApiAtencionPaciente;
import es.uclm.esi.iso2.servidor.api.ApiAuditoria;
import es.uclm.esi.iso2.servidor.api.ApiCatalogo;
import es.uclm.esi.iso2.servidor.api.ApiCitas;
import es.uclm.esi.iso2.servidor.api.ApiEsperas;
import es.uclm.esi.iso2.servidor.api.ApiEstadisticasMedico;
import es.uclm.esi.iso2.servidor.api.ApiEstadoSolicitudes;
import es.uclm.esi.iso2.servidor.api.ApiHistorial;
import es.uclm.esi.iso2.servidor.api.ApiInformes;
import es.uclm.esi.iso2.servidor.api.ApiInformesOperativos;
import es.uclm.esi.iso2.servidor.api.ApiIntegracion;
import es.uclm.esi.iso2.servidor.api.ApiMisCitas;
import es.uclm.esi.iso2.servidor.api.ApiMisSeguimientos;
import es.uclm.esi.iso2.servidor.api.ApiNotificaciones;
import es.uclm.esi.iso2.servidor.api.ApiPacientes;
import es.uclm.esi.iso2.servidor.api.ApiPermisos;
import es.uclm.esi.iso2.servidor.api.ApiRealizacion;
import es.uclm.esi.iso2.servidor.api.ApiSeguimiento;
import es.uclm.esi.iso2.servidor.api.ApiSesion;
import es.uclm.esi.iso2.servidor.api.ApiSolicitudes;
import es.uclm.esi.iso2.servidor.atencion.Encuesta;
import es.uclm.esi.iso2.servidor.atencion.IncidenciaPaciente;
import es.uclm.esi.iso2.servidor.atencion.ServicioAtencion;
import es.uclm.esi.iso2.servidor.atencion.ServicioAtencionImpl;
import es.uclm.esi.iso2.servidor.auditoria.RegistroAuditoria;
import es.uclm.esi.iso2.servidor.auditoria.ServicioAuditoria;
import es.uclm.esi.iso2.servidor.auditoria.ServicioAuditoriaImpl;
import es.uclm.esi.iso2.servidor.catalogo.ServicioCatalogo;
import es.uclm.esi.iso2.servidor.catalogo.ServicioCatalogoImpl;
import es.uclm.esi.iso2.servidor.catalogo.TipoPrueba;
import es.uclm.esi.iso2.servidor.citas.Cita;
import es.uclm.esi.iso2.servidor.citas.ServicioCitas;
import es.uclm.esi.iso2.servidor.citas.ServicioCitasImpl;
import es.uclm.esi.iso2.servidor.comun.Repositorio;
import es.uclm.esi.iso2.servidor.controlador.ControladorAgendas;
import es.uclm.esi.iso2.servidor.controlador.ControladorAtencion;
import es.uclm.esi.iso2.servidor.controlador.ControladorAtencionPaciente;
import es.uclm.esi.iso2.servidor.controlador.ControladorAuditoria;
import es.uclm.esi.iso2.servidor.controlador.ControladorCatalogo;
import es.uclm.esi.iso2.servidor.controlador.ControladorCitas;
import es.uclm.esi.iso2.servidor.controlador.ControladorEsperas;
import es.uclm.esi.iso2.servidor.controlador.ControladorEstadisticasMedico;
import es.uclm.esi.iso2.servidor.controlador.ControladorEstadoSolicitudes;
import es.uclm.esi.iso2.servidor.controlador.ControladorHistorial;
import es.uclm.esi.iso2.servidor.controlador.ControladorInformes;
import es.uclm.esi.iso2.servidor.controlador.ControladorInformesOperativos;
import es.uclm.esi.iso2.servidor.controlador.ControladorIntegracion;
import es.uclm.esi.iso2.servidor.controlador.ControladorMisCitas;
import es.uclm.esi.iso2.servidor.controlador.ControladorMisSeguimientos;
import es.uclm.esi.iso2.servidor.controlador.ControladorNotificaciones;
import es.uclm.esi.iso2.servidor.controlador.ControladorPacientes;
import es.uclm.esi.iso2.servidor.controlador.ControladorPermisos;
import es.uclm.esi.iso2.servidor.controlador.ControladorRealizacion;
import es.uclm.esi.iso2.servidor.controlador.ControladorSeguimiento;
import es.uclm.esi.iso2.servidor.controlador.ControladorSesion;
import es.uclm.esi.iso2.servidor.controlador.ControladorSolicitudes;
import es.uclm.esi.iso2.servidor.esperas.ServicioEsperas;
import es.uclm.esi.iso2.servidor.esperas.ServicioEsperasImpl;
import es.uclm.esi.iso2.servidor.estadisticas.ServicioEstadisticasMedico;
import es.uclm.esi.iso2.servidor.estadisticas.ServicioEstadisticasMedicoImpl;
import es.uclm.esi.iso2.servidor.estadisticas.ServicioInformesOperativos;
import es.uclm.esi.iso2.servidor.estadisticas.ServicioInformesOperativosImpl;
import es.uclm.esi.iso2.servidor.historial.ServicioHistorial;
import es.uclm.esi.iso2.servidor.historial.ServicioHistorialImpl;
import es.uclm.esi.iso2.servidor.informes.Informe;
import es.uclm.esi.iso2.servidor.informes.ServicioInformes;
import es.uclm.esi.iso2.servidor.informes.ServicioInformesImpl;
import es.uclm.esi.iso2.servidor.integracion.AdmisionSimulado;
import es.uclm.esi.iso2.servidor.integracion.HceSimulado;
import es.uclm.esi.iso2.servidor.integracion.PacsSimulado;
import es.uclm.esi.iso2.servidor.integracion.SsoSimulado;
import es.uclm.esi.iso2.servidor.interoperabilidad.ServicioIntegracion;
import es.uclm.esi.iso2.servidor.interoperabilidad.ServicioIntegracionImpl;
import es.uclm.esi.iso2.servidor.interoperabilidad.Sincronizacion;
import es.uclm.esi.iso2.servidor.notificaciones.Notificacion;
import es.uclm.esi.iso2.servidor.notificaciones.ServicioNotificaciones;
import es.uclm.esi.iso2.servidor.notificaciones.ServicioNotificacionesImpl;
import es.uclm.esi.iso2.servidor.notificaciones.ServicioRecordatorios;
import es.uclm.esi.iso2.servidor.pacientes.Paciente;
import es.uclm.esi.iso2.servidor.pacientes.ServicioPacientes;
import es.uclm.esi.iso2.servidor.pacientes.ServicioPacientesImpl;
import es.uclm.esi.iso2.servidor.persistencia.FabricaEnMemoria;
import es.uclm.esi.iso2.servidor.persistencia.FabricaRepositorios;
import es.uclm.esi.iso2.servidor.realizacion.Incidencia;
import es.uclm.esi.iso2.servidor.realizacion.ServicioRealizacion;
import es.uclm.esi.iso2.servidor.realizacion.ServicioRealizacionImpl;
import es.uclm.esi.iso2.servidor.seguimiento.Seguimiento;
import es.uclm.esi.iso2.servidor.seguimiento.ServicioSeguimiento;
import es.uclm.esi.iso2.servidor.seguimiento.ServicioSeguimientoImpl;
import es.uclm.esi.iso2.servidor.seguridad.ControlAcceso;
import es.uclm.esi.iso2.servidor.seguridad.PoliticaPermisos;
import es.uclm.esi.iso2.servidor.seguridad.Rol;
import es.uclm.esi.iso2.servidor.seguridad.ServicioPermisos;
import es.uclm.esi.iso2.servidor.seguridad.ServicioPermisosImpl;
import es.uclm.esi.iso2.servidor.seguridad.ServicioSesion;
import es.uclm.esi.iso2.servidor.seguridad.ServicioSesionImpl;
import es.uclm.esi.iso2.servidor.seguridad.Usuario;
import es.uclm.esi.iso2.servidor.solicitudes.ServicioSolicitudes;
import es.uclm.esi.iso2.servidor.solicitudes.ServicioSolicitudesImpl;
import es.uclm.esi.iso2.servidor.solicitudes.SolicitudPrueba;

/**
 * Raíz de composición del servidor (CONTEXT.md §3): el único sitio donde se crean las
 * implementaciones y se conectan entre sí. El cliente solo recibe las interfaces {@code api}.
 */
public class Servidor {

    private final Clock reloj;
    private final FabricaRepositorios repositorios;

    // --- infraestructura y seguridad (CU01) ---
    private final SsoSimulado sso = new SsoSimulado();
    private final PacsSimulado pacs = new PacsSimulado();
    private final HceSimulado hce = new HceSimulado();
    private final AdmisionSimulado admision = new AdmisionSimulado();
    private final Repositorio<Usuario, String> usuarios;
    private final ServicioPermisos servicioPermisos;
    private final ServicioAuditoria servicioAuditoria;
    private final ServicioSesion servicioSesion;
    private final ControlAcceso acceso;

    // --- servicios (contratos internos) ---
    private final ServicioPacientes servicioPacientes;
    private final ServicioCatalogo servicioCatalogo;
    private final ServicioSolicitudes servicioSolicitudes;
    private final ServicioAgendas servicioAgendas;
    private final ServicioCitas servicioCitas;
    private final ServicioNotificaciones servicioNotificaciones;
    private final ServicioRecordatorios servicioRecordatorios;
    private final ServicioRealizacion servicioRealizacion;
    private final ServicioInformes servicioInformes;
    private final ServicioSeguimiento servicioSeguimiento;
    private final ServicioEsperas servicioEsperas;
    private final ServicioHistorial servicioHistorial;
    private final ServicioAtencion servicioAtencion;
    private final ServicioIntegracion servicioIntegracion;
    private final ServicioEstadisticasMedico servicioEstadisticasMedico;
    private final ServicioInformesOperativos servicioInformesOperativos;

    // --- api (contratos con el cliente) ---
    private final ApiSesion apiSesion;
    private final ApiPacientes apiPacientes;
    private final ApiCatalogo apiCatalogo;
    private final ApiSolicitudes apiSolicitudes;
    private final ApiAgendas apiAgendas;
    private final ApiCitas apiCitas;
    private final ApiMisCitas apiMisCitas;
    private final ApiNotificaciones apiNotificaciones;
    private final ApiRealizacion apiRealizacion;
    private final ApiInformes apiInformes;
    private final ApiSeguimiento apiSeguimiento;
    private final ApiMisSeguimientos apiMisSeguimientos;
    private final ApiEsperas apiEsperas;
    private final ApiEstadoSolicitudes apiEstadoSolicitudes;
    private final ApiHistorial apiHistorial;
    private final ApiAtencionPaciente apiAtencionPaciente;
    private final ApiAtencion apiAtencion;
    private final ApiPermisos apiPermisos;
    private final ApiAuditoria apiAuditoria;
    private final ApiIntegracion apiIntegracion;
    private final ApiEstadisticasMedico apiEstadisticasMedico;
    private final ApiInformesOperativos apiInformesOperativos;

    /** Servidor con los datos en memoria (pruebas). */
    public Servidor(Clock reloj) {
        this(reloj, new FabricaEnMemoria());
    }

    /** Servidor que guarda sus datos con la fábrica indicada (memoria o MySQL, D-018). */
    public Servidor(Clock reloj, FabricaRepositorios repositorios) {
        this.reloj = reloj;
        this.repositorios = repositorios;
        this.usuarios = repositorios.crear("usuarios", Usuario::login);
        this.servicioSesion = new ServicioSesionImpl(sso, usuarios, reloj);
        this.servicioPermisos = new ServicioPermisosImpl(
                repositorios.crear("politica_permisos", (PoliticaPermisos p) -> ServicioPermisosImpl.ID_POLITICA),
                usuarios);
        this.servicioAuditoria = new ServicioAuditoriaImpl(repositorios.crear("auditoria", RegistroAuditoria::id), reloj);
        this.acceso = new ControlAcceso(servicioSesion, servicioPermisos, servicioAuditoria);

        // servicios
        this.servicioPacientes = new ServicioPacientesImpl(repositorios.crear("pacientes", Paciente::getId), reloj);
        this.servicioCatalogo = new ServicioCatalogoImpl(repositorios.crear("tipos_prueba", TipoPrueba::codigo),
                repositorios.crear("parametros", p -> ServicioCatalogoImpl.ID_PARAMETROS));
        this.servicioSolicitudes = new ServicioSolicitudesImpl(
                repositorios.crear("solicitudes", SolicitudPrueba::getId), servicioPacientes, servicioCatalogo, reloj);
        this.servicioAgendas = new ServicioAgendasImpl(repositorios.crear("agendas", Agenda::getId),
                servicioCatalogo.parametros(), reloj);
        this.servicioNotificaciones = new ServicioNotificacionesImpl(
                repositorios.crear("notificaciones", Notificacion::getId), reloj);
        this.servicioCitas = new ServicioCitasImpl(repositorios.crear("citas", Cita::getId), servicioSolicitudes,
                servicioAgendas, servicioCatalogo, servicioNotificaciones, reloj);
        this.servicioRecordatorios = new ServicioRecordatorios(servicioCitas, servicioSolicitudes, servicioCatalogo,
                servicioNotificaciones, repositorios.crear("recordatorios_enviados", (Long id) -> id), reloj);
        this.servicioRealizacion = new ServicioRealizacionImpl(servicioCitas, servicioSolicitudes, servicioAgendas,
                pacs, repositorios.crear("incidencias", Incidencia::id), reloj);
        this.servicioInformes = new ServicioInformesImpl(repositorios.crear("informes", Informe::id),
                servicioSolicitudes, servicioCitas, servicioAgendas, pacs, servicioNotificaciones, reloj);
        this.servicioSeguimiento = new ServicioSeguimientoImpl(repositorios.crear("seguimientos", Seguimiento::getId),
                servicioPacientes, servicioCatalogo, servicioSolicitudes, servicioNotificaciones, reloj);
        this.servicioEsperas = new ServicioEsperasImpl(servicioSolicitudes, servicioCatalogo, servicioAgendas,
                servicioCitas, reloj);
        this.servicioHistorial = new ServicioHistorialImpl(servicioSolicitudes, servicioCatalogo, servicioInformes);
        this.servicioAtencion = new ServicioAtencionImpl(
                repositorios.crear("incidencias_paciente", IncidenciaPaciente::getId),
                repositorios.crear("encuestas", Encuesta::idSolicitud), servicioSolicitudes, reloj);
        this.servicioIntegracion = new ServicioIntegracionImpl(List.of(sso, hce, admision, pacs), admision, hce,
                servicioPacientes, servicioInformes, repositorios.crear("informes_enviados_hce", (Long id) -> id),
                repositorios.crear("sincronizaciones", Sincronizacion::id), reloj);
        this.servicioEstadisticasMedico = new ServicioEstadisticasMedicoImpl(servicioSolicitudes, servicioCatalogo,
                servicioSeguimiento);
        this.servicioInformesOperativos = new ServicioInformesOperativosImpl(servicioAgendas, servicioCitas,
                servicioSolicitudes, servicioCatalogo);

        // api
        this.apiSesion = new ControladorSesion(servicioSesion, acceso);
        this.apiPacientes = new ControladorPacientes(servicioPacientes, acceso);
        this.apiCatalogo = new ControladorCatalogo(servicioCatalogo, acceso);
        this.apiSolicitudes = new ControladorSolicitudes(servicioSolicitudes, acceso);
        this.apiAgendas = new ControladorAgendas(servicioAgendas, acceso);
        this.apiCitas = new ControladorCitas(servicioCitas, servicioSolicitudes, acceso);
        this.apiMisCitas = new ControladorMisCitas(servicioCitas, acceso);
        this.apiNotificaciones = new ControladorNotificaciones(servicioNotificaciones, servicioRecordatorios, acceso);
        this.apiRealizacion = new ControladorRealizacion(servicioRealizacion, acceso);
        this.apiInformes = new ControladorInformes(servicioInformes, servicioSolicitudes, acceso);
        this.apiSeguimiento = new ControladorSeguimiento(servicioSeguimiento, acceso);
        this.apiMisSeguimientos = new ControladorMisSeguimientos(servicioSeguimiento, servicioSolicitudes, acceso);
        this.apiEsperas = new ControladorEsperas(servicioEsperas, acceso);
        this.apiEstadoSolicitudes = new ControladorEstadoSolicitudes(servicioSolicitudes, servicioCitas,
                servicioAgendas, acceso);
        this.apiHistorial = new ControladorHistorial(servicioHistorial, acceso);
        this.apiAtencionPaciente = new ControladorAtencionPaciente(servicioAtencion, acceso);
        this.apiAtencion = new ControladorAtencion(servicioAtencion, acceso);
        this.apiPermisos = new ControladorPermisos(servicioPermisos, acceso);
        this.apiAuditoria = new ControladorAuditoria(servicioAuditoria, acceso);
        this.apiIntegracion = new ControladorIntegracion(servicioIntegracion, acceso);
        this.apiEstadisticasMedico = new ControladorEstadisticasMedico(servicioEstadisticasMedico, acceso);
        this.apiInformesOperativos = new ControladorInformesOperativos(servicioInformesOperativos, acceso);
    }

    /**
     * Servidor con el reloj del sistema, guardando en MySQL si está configurado (RADIO_DB_URL…) y con los
     * datos de demostración.
     */
    public static Servidor conDatosDemo() {
        return conDatosDemo(Clock.systemDefaultZone(), FabricaRepositorios.segunEntorno());
    }

    public static Servidor conDatosDemo(Clock reloj) {
        return conDatosDemo(reloj, new FabricaEnMemoria());
    }

    /** Las cuentas del SSO simulado se dan de alta siempre; el resto de datos, solo si el almacén está vacío. */
    public static Servidor conDatosDemo(Clock reloj, FabricaRepositorios repositorios) {
        Servidor servidor = new Servidor(reloj, repositorios);
        DatosDemo.cargar(servidor);
        return servidor;
    }

    /** Dónde se guardan los datos (para informar al usuario). */
    public String almacenamiento() {
        return repositorios.descripcion();
    }

    /** No hay ningún usuario guardado: es el primer arranque con este almacén. */
    boolean sinDatos() {
        return usuarios.todos().isEmpty();
    }

    /** Da de alta una cuenta en el SSO simulado (sistema externo: no se guarda en nuestra base de datos). */
    void registrarCuentaSso(String login, String contrasena, String nombre) {
        sso.registrar(login, contrasena, nombre);
    }

    /** Da de alta una cuenta en el SSO simulado y su usuario en la aplicación. */
    public Usuario registrarUsuario(String login, String contrasena, String nombre, Rol rol, String idPaciente) {
        sso.registrar(login, contrasena, nombre);
        return usuarios.guardar(new Usuario(login, nombre, rol, idPaciente));
    }

    public Clock reloj() {
        return reloj;
    }

    // --- api ---
    public ApiSesion sesion() {
        return apiSesion;
    }

    public ApiPacientes pacientes() {
        return apiPacientes;
    }

    public ApiCatalogo catalogo() {
        return apiCatalogo;
    }

    public ApiSolicitudes solicitudes() {
        return apiSolicitudes;
    }

    public ApiAgendas agendas() {
        return apiAgendas;
    }

    public ApiCitas citas() {
        return apiCitas;
    }

    public ApiMisCitas misCitas() {
        return apiMisCitas;
    }

    public ApiNotificaciones notificaciones() {
        return apiNotificaciones;
    }

    public ApiRealizacion realizacion() {
        return apiRealizacion;
    }

    public ApiInformes informes() {
        return apiInformes;
    }

    public ApiSeguimiento seguimiento() {
        return apiSeguimiento;
    }

    public ApiMisSeguimientos misSeguimientos() {
        return apiMisSeguimientos;
    }

    public ApiEsperas esperas() {
        return apiEsperas;
    }

    public ApiEstadoSolicitudes estadoSolicitudes() {
        return apiEstadoSolicitudes;
    }

    public ApiHistorial historial() {
        return apiHistorial;
    }

    public ApiAtencionPaciente atencionPaciente() {
        return apiAtencionPaciente;
    }

    public ApiAtencion atencion() {
        return apiAtencion;
    }

    public ApiPermisos permisos() {
        return apiPermisos;
    }

    public ApiAuditoria auditoria() {
        return apiAuditoria;
    }

    public ApiIntegracion integracion() {
        return apiIntegracion;
    }

    public ApiEstadisticasMedico estadisticasMedico() {
        return apiEstadisticasMedico;
    }

    public ApiInformesOperativos informesOperativos() {
        return apiInformesOperativos;
    }

    // --- servicios internos (datos de demostración) ---
    ServicioPacientes servicioPacientes() {
        return servicioPacientes;
    }

    ServicioCatalogo servicioCatalogo() {
        return servicioCatalogo;
    }

    ServicioSolicitudes servicioSolicitudes() {
        return servicioSolicitudes;
    }

    ServicioAgendas servicioAgendas() {
        return servicioAgendas;
    }

    ServicioCitas servicioCitas() {
        return servicioCitas;
    }

    ServicioRealizacion servicioRealizacion() {
        return servicioRealizacion;
    }

    ServicioInformes servicioInformes() {
        return servicioInformes;
    }

    ServicioSeguimiento servicioSeguimiento() {
        return servicioSeguimiento;
    }

    // --- sistemas externos simulados (datos de demostración y pruebas de tolerancia a fallos, CU19) ---
    public PacsSimulado pacs() {
        return pacs;
    }

    public HceSimulado hce() {
        return hce;
    }

    public AdmisionSimulado admision() {
        return admision;
    }

    public SsoSimulado sso() {
        return sso;
    }
}
