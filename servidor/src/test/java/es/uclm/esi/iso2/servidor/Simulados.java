package es.uclm.esi.iso2.servidor;

import es.uclm.esi.iso2.servidor.integracion.AdmisionSimulado;
import es.uclm.esi.iso2.servidor.integracion.HceSimulado;
import es.uclm.esi.iso2.servidor.integracion.PacsSimulado;
import es.uclm.esi.iso2.servidor.integracion.SsoSimulado;

/**
 * Acceso de las pruebas a los sistemas externos simulados de un servidor (p. ej. para simular una caída,
 * CU19). Está en el código de pruebas para que {@link Servidor} no los exponga como API pública.
 */
public final class Simulados {

    private Simulados() {
    }

    public static PacsSimulado pacs(Servidor s) {
        return s.pacs();
    }

    public static HceSimulado hce(Servidor s) {
        return s.hce();
    }

    public static AdmisionSimulado admision(Servidor s) {
        return s.admision();
    }

    public static SsoSimulado sso(Servidor s) {
        return s.sso();
    }
}
