package es.uclm.esi.iso2.servidor.integracion;

/** Historia clínica electrónica (HCE) del SESCAM (D-009). Recibe los informes radiológicos. */
public interface SistemaHce extends SistemaExterno {

    @Override
    default String nombre() {
        return "HCE";
    }

    /**
     * Publica un documento en la historia del paciente.
     *
     * @throws es.uclm.esi.iso2.servidor.comun.ReglaNegocioException si la HCE no está disponible
     */
    void publicar(DocumentoHce documento);
}
