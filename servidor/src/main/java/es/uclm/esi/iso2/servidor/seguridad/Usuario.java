package es.uclm.esi.iso2.servidor.seguridad;

import es.uclm.esi.iso2.servidor.comun.ReglaNegocioException;
import es.uclm.esi.iso2.servidor.comun.Validaciones;

/**
 * Usuario de la aplicación. El SSO lo autentica; el rol y el vínculo con un paciente se guardan aquí.
 *
 * @param idPaciente identificador del paciente asociado (obligatorio para el rol PACIENTE; opcional
 *                   para el resto, p. ej. un médico que también es paciente)
 */
public record Usuario(String login, String nombre, Rol rol, String idPaciente) {

    public Usuario {
        login = Validaciones.texto(login, "login");
        nombre = Validaciones.texto(nombre, "nombre");
        Validaciones.obligatorio(rol, "rol");
        if (rol == Rol.PACIENTE && (idPaciente == null || idPaciente.isBlank())) {
            throw new IllegalArgumentException("Un usuario paciente debe estar asociado a un paciente");
        }
    }

    /** Id del paciente asociado; lanza ReglaNegocioException si el usuario no es paciente de ningún registro. */
    public String exigirPaciente() {
        if (idPaciente == null || idPaciente.isBlank()) {
            throw new ReglaNegocioException("El usuario " + login + " no tiene un paciente asociado");
        }
        return idPaciente;
    }

    public Usuario conRol(Rol nuevoRol) {
        return new Usuario(login, nombre, nuevoRol, idPaciente);
    }
}
