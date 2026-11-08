package es.uclm.esi.iso2.servidor.integracion;

/**
 * Referencia a una serie de imágenes almacenada en el PACS.
 *
 * @param studyInstanceUid identificador DICOM del estudio (RNF-C2)
 */
public record ImagenDicom(String studyInstanceUid, int numeroSerie, String descripcion, int numeroImagenes) {
}
