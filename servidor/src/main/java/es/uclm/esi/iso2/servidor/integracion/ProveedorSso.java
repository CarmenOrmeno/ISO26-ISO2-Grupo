package es.uclm.esi.iso2.servidor.integracion;

import java.util.Optional;

/** Inicio de sesión único (SSO) corporativo del SESCAM (D-009). Solo autentica; no autoriza. */
public interface ProveedorSso extends SistemaExterno {

    @Override
    default String nombre() {
        return "SSO corporativo";
    }

    /**
     * Devuelve la identidad si las credenciales son correctas; vacío en otro caso.
     *
     * @throws es.uclm.esi.iso2.servidor.comun.ReglaNegocioException si el SSO no está disponible
     */
    Optional<IdentidadSso> autenticar(String login, String contrasena);
}
