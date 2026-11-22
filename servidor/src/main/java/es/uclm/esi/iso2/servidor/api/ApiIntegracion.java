package es.uclm.esi.iso2.servidor.api;

import java.util.List;

import es.uclm.esi.iso2.servidor.interoperabilidad.EstadoSistema;
import es.uclm.esi.iso2.servidor.interoperabilidad.Sincronizacion;

/** CU19 · Integrar con SSO, HCE, admisión y RIS/PACS (administrador). */
public interface ApiIntegracion {

    List<EstadoSistema> estado(String token);

    Sincronizacion importarPacientes(String token);

    Sincronizacion enviarInformesHce(String token);

    List<Sincronizacion> sincronizaciones(String token);
}
