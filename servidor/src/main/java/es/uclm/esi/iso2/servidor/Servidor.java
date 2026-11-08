package es.uclm.esi.iso2.servidor;

import java.time.Clock;

import es.uclm.esi.iso2.servidor.api.ApiPacientes;
import es.uclm.esi.iso2.servidor.api.ApiSesion;
import es.uclm.esi.iso2.servidor.comun.Repositorio;
import es.uclm.esi.iso2.servidor.comun.RepositorioEnMemoria;
import es.uclm.esi.iso2.servidor.controlador.ControladorPacientes;
import es.uclm.esi.iso2.servidor.controlador.ControladorSesion;
import es.uclm.esi.iso2.servidor.integracion.SsoSimulado;
import es.uclm.esi.iso2.servidor.pacientes.Paciente;
import es.uclm.esi.iso2.servidor.pacientes.ServicioPacientes;
import es.uclm.esi.iso2.servidor.pacientes.ServicioPacientesImpl;
import es.uclm.esi.iso2.servidor.seguridad.ControlAcceso;
import es.uclm.esi.iso2.servidor.seguridad.PoliticaPermisos;
import es.uclm.esi.iso2.servidor.seguridad.Rol;
import es.uclm.esi.iso2.servidor.seguridad.ServicioSesion;
import es.uclm.esi.iso2.servidor.seguridad.ServicioSesionImpl;
import es.uclm.esi.iso2.servidor.seguridad.Usuario;

/**
 * Raíz de composición del servidor (CONTEXT.md §3): el único sitio donde se crean las
 * implementaciones y se conectan entre sí. El cliente solo recibe las interfaces {@code api}.
 */
public class Servidor {

    private final Clock reloj;
    private final SsoSimulado sso = new SsoSimulado();
    private final Repositorio<Usuario, String> usuarios = new RepositorioEnMemoria<>(Usuario::login);
    private final PoliticaPermisos politica = PoliticaPermisos.porDefecto();
    private final ServicioSesion servicioSesion;
    private final ControlAcceso acceso;
    private final ServicioPacientes servicioPacientes;

    private final ApiSesion apiSesion;
    private final ApiPacientes apiPacientes;

    public Servidor(Clock reloj) {
        this.reloj = reloj;
        this.servicioSesion = new ServicioSesionImpl(sso, usuarios, reloj);
        this.acceso = new ControlAcceso(servicioSesion, politica);
        this.servicioPacientes = new ServicioPacientesImpl(new RepositorioEnMemoria<>(Paciente::getId), reloj);

        this.apiSesion = new ControladorSesion(servicioSesion, acceso);
        this.apiPacientes = new ControladorPacientes(servicioPacientes, acceso);
    }

    /** Servidor con el reloj del sistema y los datos de demostración cargados. */
    public static Servidor conDatosDemo() {
        return conDatosDemo(Clock.systemDefaultZone());
    }

    public static Servidor conDatosDemo(Clock reloj) {
        Servidor servidor = new Servidor(reloj);
        DatosDemo.cargar(servidor);
        return servidor;
    }

    /** Da de alta una cuenta en el SSO simulado y su usuario en la aplicación. */
    public Usuario registrarUsuario(String login, String contrasena, String nombre, Rol rol, String idPaciente) {
        sso.registrar(login, contrasena, nombre);
        return usuarios.guardar(new Usuario(login, nombre, rol, idPaciente));
    }

    public Clock reloj() {
        return reloj;
    }

    public ApiSesion sesion() {
        return apiSesion;
    }

    public ApiPacientes pacientes() {
        return apiPacientes;
    }

    ServicioPacientes servicioPacientes() {
        return servicioPacientes;
    }
}
