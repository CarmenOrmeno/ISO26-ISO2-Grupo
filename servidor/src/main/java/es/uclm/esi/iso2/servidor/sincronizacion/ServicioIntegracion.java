package es.uclm.esi.iso2.servidor.sincronizacion;

import java.util.List;

/**
 * Contrato interno de la integración con los sistemas hospitalarios (CU19). Si un sistema no responde,
 * la sincronización no lanza excepción: queda guardada como fallida y se puede repetir.
 */
public interface ServicioIntegracion {

    /** SSO, HCE, Admisión y RIS/PACS, con su disponibilidad. */
    List<EstadoSistema> estado();

    /** Da de alta los pacientes de admisión cuyo DNI aún no está en la aplicación. */
    Sincronizacion importarPacientes();

    /** Publica en la HCE los informes que aún no se han enviado (cada informe se envía una sola vez). */
    Sincronizacion enviarInformesHce();

    /** Sincronizaciones hechas, de la más reciente a la más antigua. */
    List<Sincronizacion> sincronizaciones();
}
