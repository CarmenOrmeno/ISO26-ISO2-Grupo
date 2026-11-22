package es.uclm.esi.iso2.servidor.interoperabilidad;

/** Intercambios de datos con los sistemas hospitalarios que lanza el administrador (CU19). */
public enum TipoSincronizacion {
    /** Admisión → aplicación: alta de los pacientes que aún no existen (por DNI). */
    IMPORTAR_PACIENTES,
    /** Aplicación → HCE: publicación de los informes radiológicos aún no enviados. */
    ENVIAR_INFORMES
}
