package es.uclm.esi.iso2.servidor.seguridad;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import es.uclm.esi.iso2.servidor.comun.ReglaNegocioException;
import es.uclm.esi.iso2.servidor.comun.Repositorio;
import es.uclm.esi.iso2.servidor.comun.Validaciones;

/** Guarda la política en su almacén tras cada cambio, para que se conserve al reiniciar (D-018). */
public class ServicioPermisosImpl implements ServicioPermisos {

    /** Id de la única política del sistema en su almacén. */
    public static final String ID_POLITICA = "sistema";

    private final Repositorio<PoliticaPermisos, String> almacen;
    private final Repositorio<Usuario, String> usuarios;
    private final PoliticaPermisos politica;

    /** Usa la política guardada; si no hay ninguna, guarda la de por defecto. */
    public ServicioPermisosImpl(Repositorio<PoliticaPermisos, String> almacen, Repositorio<Usuario, String> usuarios) {
        this.almacen = almacen;
        this.usuarios = usuarios;
        this.politica = almacen.buscar(ID_POLITICA).orElseGet(() -> almacen.guardar(PoliticaPermisos.porDefecto()));
    }

    @Override
    public boolean tiene(Rol rol, Permiso permiso) {
        return politica.tiene(rol, permiso);
    }

    @Override
    public Set<Permiso> permisosDe(Rol rol) {
        return politica.permisosDe(rol);
    }

    @Override
    public void conceder(Rol rol, Permiso permiso) {
        politica.conceder(rol, permiso);
        almacen.guardar(politica);
    }

    @Override
    public void revocar(Rol rol, Permiso permiso) {
        if (rol == Rol.ADMINISTRADOR && permiso == Permiso.GESTIONAR_PERMISOS) {
            throw new ReglaNegocioException("El administrador no puede perder el permiso de gestionar permisos");
        }
        politica.revocar(rol, permiso);
        almacen.guardar(politica);
    }

    @Override
    public void restablecer() {
        politica.restablecer();
        almacen.guardar(politica);
    }

    @Override
    public List<Usuario> usuarios() {
        return usuarios.todos().stream().sorted(Comparator.comparing(Usuario::login)).toList();
    }

    @Override
    public Usuario cambiarRol(String loginQuienCambia, String login, Rol nuevoRol) {
        Validaciones.obligatorio(nuevoRol, "rol");
        if (login.equals(loginQuienCambia)) {
            throw new ReglaNegocioException("Nadie puede cambiar su propio rol");
        }
        Usuario usuario = usuarios.obtener(login);
        return usuarios.guardar(usuario.conRol(nuevoRol));
    }
}
