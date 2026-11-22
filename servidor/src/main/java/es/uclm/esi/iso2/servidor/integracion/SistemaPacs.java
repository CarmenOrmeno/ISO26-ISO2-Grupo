package es.uclm.esi.iso2.servidor.integracion;

import java.util.List;

/** Plataforma RIS/PACS del hospital (D-009). Almacena y devuelve los estudios de imagen (DICOM). */
public interface SistemaPacs {

    /** Registra el estudio adquirido en la prueba y devuelve su Study Instance UID. */
    String registrarEstudio(long idSolicitud, String idPaciente, String modalidad, int numeroImagenes);

    /** Series de imágenes del estudio asociado a una solicitud (vacío si no hay). */
    List<ImagenDicom> imagenes(long idSolicitud);

    boolean disponible();
}
