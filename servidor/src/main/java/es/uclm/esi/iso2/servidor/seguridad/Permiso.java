package es.uclm.esi.iso2.servidor.seguridad;

import static es.uclm.esi.iso2.servidor.seguridad.Rol.ADMINISTRADOR;
import static es.uclm.esi.iso2.servidor.seguridad.Rol.ADMINISTRATIVO;
import static es.uclm.esi.iso2.servidor.seguridad.Rol.MEDICO;
import static es.uclm.esi.iso2.servidor.seguridad.Rol.PACIENTE;

import java.util.EnumSet;
import java.util.Set;

/**
 * Operaciones protegidas de la API (D-011). Cada permiso declara si accede a datos clínicos (se
 * audita, RNF-S2) y a qué roles se concede en la política por defecto. Añadir un permiso no obliga a
 * modificar {@link PoliticaPermisos} (principio abierto/cerrado).
 */
public enum Permiso {
    // CU01
    CONSULTAR_PERFIL(false, PACIENTE, ADMINISTRATIVO, ADMINISTRADOR),
    // CU02
    GESTIONAR_PACIENTES(false, ADMINISTRATIVO),
    CONSULTAR_PACIENTES(false, ADMINISTRATIVO, MEDICO),
    // CU03
    CONSULTAR_CATALOGO(false, PACIENTE, ADMINISTRATIVO, ADMINISTRADOR),
    CONFIGURAR_SISTEMA(false, ADMINISTRADOR),
    // CU04
    SOLICITAR_PRUEBAS(true, MEDICO),
    CONSULTAR_SOLICITUDES(true, MEDICO, ADMINISTRATIVO),
    // CU05
    GESTIONAR_AGENDAS(false, ADMINISTRATIVO),
    CONSULTAR_AGENDAS(false, ADMINISTRATIVO),
    // CU06
    GESTIONAR_CITAS(false, ADMINISTRATIVO),
    // It7: CU07 y CU12
    GESTIONAR_MIS_CITAS(false, PACIENTE),
    CONSULTAR_MIS_NOTIFICACIONES(false, PACIENTE),
    EJECUTAR_TAREAS_PROGRAMADAS(false, ADMINISTRADOR),
    // It8: CU08 y CU09
    REGISTRAR_REALIZACION(true, MEDICO),
    CREAR_INFORMES(true, MEDICO),
    CONSULTAR_INFORMES_PACIENTES(true, MEDICO),
    // It9: CU10 y CU16
    PRESCRIBIR_SEGUIMIENTOS(true, MEDICO),
    CONSULTAR_MIS_SEGUIMIENTOS(true, PACIENTE),
    // It10: CU11 y CU14
    GESTIONAR_LISTAS_ESPERA(true, ADMINISTRATIVO),
    CONSULTAR_ESTADO_SOLICITUDES(true, MEDICO),
    CONSULTAR_DISPONIBILIDAD(false, MEDICO),
    // It11: CU13 y CU15
    CONSULTAR_MI_HISTORIAL(true, PACIENTE),
    COMUNICAR_INCIDENCIAS(false, PACIENTE),
    RESPONDER_ENCUESTAS(false, PACIENTE),
    ATENDER_INCIDENCIAS(false, ADMINISTRATIVO);

    private final boolean datosClinicos;
    private final Set<Rol> rolesPorDefecto;

    Permiso(boolean datosClinicos, Rol primero, Rol... resto) {
        this.datosClinicos = datosClinicos;
        this.rolesPorDefecto = EnumSet.of(primero, resto);
    }

    public boolean accedeADatosClinicos() {
        return datosClinicos;
    }

    /** Roles que tienen el permiso en la política por defecto (el médico hereda los del paciente). */
    public Set<Rol> rolesPorDefecto() {
        return EnumSet.copyOf(rolesPorDefecto);
    }
}
