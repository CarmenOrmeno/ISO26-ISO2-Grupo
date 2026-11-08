package es.uclm.esi.iso2.servidor.seguridad;

import java.time.Duration;
import java.time.LocalDateTime;

/** Sesión abierta de un usuario. Caduca tras un periodo de inactividad. */
public class Sesion {

    private final String token;
    private final String login;
    private final LocalDateTime inicio;
    private LocalDateTime ultimoAcceso;

    public Sesion(String token, String login, LocalDateTime inicio) {
        this.token = token;
        this.login = login;
        this.inicio = inicio;
        this.ultimoAcceso = inicio;
    }

    /** Una sesión caduca cuando han pasado {@code inactividadMaxima} o más desde el último acceso. */
    public boolean caducada(LocalDateTime ahora, Duration inactividadMaxima) {
        return !ahora.isBefore(ultimoAcceso.plus(inactividadMaxima));
    }

    public void registrarAcceso(LocalDateTime ahora) {
        this.ultimoAcceso = ahora;
    }

    public String getToken() {
        return token;
    }

    public String getLogin() {
        return login;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public LocalDateTime getUltimoAcceso() {
        return ultimoAcceso;
    }
}
