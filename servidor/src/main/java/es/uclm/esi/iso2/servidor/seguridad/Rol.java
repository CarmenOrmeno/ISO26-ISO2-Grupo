package es.uclm.esi.iso2.servidor.seguridad;

/** Tipos de usuario del sistema. El médico hereda las funcionalidades del paciente (D-005). */
public enum Rol {
    PACIENTE,
    MEDICO,
    ADMINISTRATIVO,
    ADMINISTRADOR
}
