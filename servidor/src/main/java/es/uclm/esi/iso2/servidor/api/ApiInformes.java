package es.uclm.esi.iso2.servidor.api;

import java.util.List;

import es.uclm.esi.iso2.servidor.informes.Informe;
import es.uclm.esi.iso2.servidor.integracion.ImagenDicom;

/** CU09 · Crear informes y consultar informes e imágenes de sus pacientes (médico). */
public interface ApiInformes {

    Informe crearInforme(String token, long idSolicitud, String hallazgos, String conclusion);

    Informe informeDeSolicitud(String token, long idSolicitud);

    List<Informe> informesDePaciente(String token, String idPaciente);

    List<ImagenDicom> imagenes(String token, long idSolicitud);
}
