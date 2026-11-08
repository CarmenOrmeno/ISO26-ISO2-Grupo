package es.uclm.esi.iso2.servidor.integracion;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** SSO simulado en memoria para desarrollo y pruebas. */
public class SsoSimulado implements ProveedorSso {

    private record Cuenta(String contrasena, String nombreCompleto) {
    }

    private final Map<String, Cuenta> cuentas = new HashMap<>();

    public SsoSimulado registrar(String login, String contrasena, String nombreCompleto) {
        cuentas.put(login, new Cuenta(contrasena, nombreCompleto));
        return this;
    }

    @Override
    public Optional<IdentidadSso> autenticar(String login, String contrasena) {
        if (login == null || contrasena == null) {
            return Optional.empty();
        }
        Cuenta cuenta = cuentas.get(login);
        if (cuenta == null || !cuenta.contrasena().equals(contrasena)) {
            return Optional.empty();
        }
        return Optional.of(new IdentidadSso(login, cuenta.nombreCompleto()));
    }
}
