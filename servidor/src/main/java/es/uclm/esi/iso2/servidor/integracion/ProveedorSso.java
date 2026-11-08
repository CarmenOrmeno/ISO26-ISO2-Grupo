package es.uclm.esi.iso2.servidor.integracion;

import java.util.Optional;

/** Inicio de sesión único (SSO) corporativo del SESCAM (D-009). Solo autentica; no autoriza. */
public interface ProveedorSso {

    /** Devuelve la identidad si las credenciales son correctas; vacío en otro caso. */
    Optional<IdentidadSso> autenticar(String login, String contrasena);
}
