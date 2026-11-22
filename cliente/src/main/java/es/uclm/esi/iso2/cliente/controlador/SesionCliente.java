package es.uclm.esi.iso2.cliente.controlador;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import es.uclm.esi.iso2.servidor.api.ApiSesion;
import es.uclm.esi.iso2.servidor.seguridad.Rol;
import es.uclm.esi.iso2.servidor.seguridad.Usuario;

/**
 * Controlador de la sesión del cliente (CU01), común a las dos vistas (JavaFX y consola): inicia y cierra la
 * sesión, guarda el token y da las acciones del rol del usuario.
 */
public class SesionCliente {

    private final ApiSesion api;
    private final Map<Rol, List<Accion>> catalogo;
    private String token;
    private Usuario usuario;

    public SesionCliente(ApiSesion api, Map<Rol, List<Accion>> catalogo) {
        this.api = api;
        this.catalogo = new EnumMap<>(catalogo);
    }

    /**
     * Inicia la sesión. Si ya había una, la cierra antes.
     *
     * @throws RuntimeException con el mensaje para el usuario si no se puede iniciar (credenciales, SSO caído…)
     */
    public Usuario iniciar(String login, String contrasena) {
        cerrar();
        String nuevo = api.iniciarSesion(login, contrasena);
        token = nuevo;
        usuario = api.usuarioActual(nuevo);
        return usuario;
    }

    public boolean iniciada() {
        return token != null;
    }

    public Usuario usuario() {
        exigirIniciada();
        return usuario;
    }

    /** Acciones del rol del usuario, en el orden del catálogo. */
    public List<Accion> acciones() {
        exigirIniciada();
        return catalogo.getOrDefault(usuario.rol(), List.of());
    }

    /** Acciones agrupadas por caso de uso, conservando el orden. */
    public Map<String, List<Accion>> accionesPorGrupo() {
        Map<String, List<Accion>> grupos = new LinkedHashMap<>();
        for (Accion a : acciones()) {
            grupos.computeIfAbsent(a.grupo(), g -> new java.util.ArrayList<>()).add(a);
        }
        return grupos;
    }

    public Resultado ejecutar(Accion accion, List<String> textos) {
        exigirIniciada();
        return accion.ejecutar(token, textos);
    }

    public void cerrar() {
        if (token != null) {
            try {
                api.cerrarSesion(token);
            } finally {
                token = null;
                usuario = null;
            }
        }
    }

    private void exigirIniciada() {
        if (token == null) {
            throw new IllegalStateException("No hay ninguna sesión iniciada");
        }
    }
}
