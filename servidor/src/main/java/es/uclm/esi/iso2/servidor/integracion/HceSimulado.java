package es.uclm.esi.iso2.servidor.integracion;

import java.util.ArrayList;
import java.util.List;

import es.uclm.esi.iso2.servidor.comun.ReglaNegocioException;

/** HCE simulada en memoria: guarda los documentos publicados. */
public class HceSimulado implements SistemaHce {

    private final List<DocumentoHce> documentos = new ArrayList<>();
    private boolean disponible = true;

    @Override
    public void publicar(DocumentoHce documento) {
        if (!disponible) {
            throw new ReglaNegocioException("La HCE no está disponible");
        }
        documentos.add(documento);
    }

    /** Documentos publicados en la historia de un paciente (para comprobar la integración). */
    public List<DocumentoHce> documentos(String dniPaciente) {
        return documentos.stream().filter(d -> d.dniPaciente().equals(dniPaciente)).toList();
    }

    @Override
    public boolean disponible() {
        return disponible;
    }

    /** Para pruebas de tolerancia a fallos: simula una caída de la HCE. */
    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }
}
