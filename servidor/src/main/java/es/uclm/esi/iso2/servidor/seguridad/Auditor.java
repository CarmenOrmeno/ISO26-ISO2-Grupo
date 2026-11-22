package es.uclm.esi.iso2.servidor.seguridad;

/**
 * Lo que necesita {@link ControlAcceso} para dejar constancia de los accesos (RNF-S2, CU18). La interfaz es
 * de la seguridad y la implementa el componente de auditoría (inversión de dependencias, sin ciclo).
 */
@FunctionalInterface
public interface Auditor {

    /**
     * @param recurso   sobre qué se pide el acceso (p. ej. «informe de la solicitud 7»); vacío si no se indica
     * @param permitido si el rol del usuario tenía el permiso
     */
    void registrar(Usuario usuario, Permiso permiso, String recurso, boolean permitido);
}
