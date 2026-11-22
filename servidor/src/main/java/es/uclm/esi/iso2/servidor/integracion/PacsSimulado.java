package es.uclm.esi.iso2.servidor.integracion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.uclm.esi.iso2.servidor.comun.ReglaNegocioException;

/** PACS simulado en memoria. Genera UID con la raíz de ejemplo 1.2.826.0.1.3680043.10.999 (no oficial). */
public class PacsSimulado implements SistemaPacs {

    static final String RAIZ_UID = "1.2.826.0.1.3680043.10.999";

    private final Map<Long, List<ImagenDicom>> estudios = new HashMap<>();
    private boolean disponible = true;

    @Override
    public String registrarEstudio(long idSolicitud, String idPaciente, String modalidad, int numeroImagenes) {
        comprobarDisponible();
        if (numeroImagenes < 1) {
            throw new IllegalArgumentException("Un estudio debe tener al menos una imagen");
        }
        String uid = RAIZ_UID + "." + idSolicitud;
        List<ImagenDicom> series = new ArrayList<>();
        series.add(new ImagenDicom(uid, 1, modalidad + " de " + idPaciente, numeroImagenes));
        estudios.put(idSolicitud, series);
        return uid;
    }

    @Override
    public List<ImagenDicom> imagenes(long idSolicitud) {
        comprobarDisponible();
        return List.copyOf(estudios.getOrDefault(idSolicitud, List.of()));
    }

    @Override
    public boolean disponible() {
        return disponible;
    }

    /** Para pruebas de tolerancia a fallos: simula una caída del PACS. */
    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    private void comprobarDisponible() {
        if (!disponible) {
            throw new ReglaNegocioException("El PACS no está disponible");
        }
    }
}
