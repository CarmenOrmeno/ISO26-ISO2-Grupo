package es.uclm.esi.iso2.servidor.informes;

import java.util.List;
import java.util.Optional;

import es.uclm.esi.iso2.servidor.integracion.ImagenDicom;

/** Contrato interno de informes e imágenes (CU09). */
public interface ServicioInformes {

    /** Crea el informe de una solicitud REALIZADA; la solicitud pasa a INFORMADA y se avisa al paciente. */
    Informe crearInforme(long idSolicitud, String loginMedico, String hallazgos, String conclusion);

    Optional<Informe> informeDeSolicitud(long idSolicitud);

    List<Informe> informesDePaciente(String idPaciente);

    List<ImagenDicom> imagenes(long idSolicitud);

    /** El médico atiende al paciente si le ha pedido alguna prueba o tiene alguna en sus agendas. */
    boolean esPacienteDe(String loginMedico, String idPaciente);

    List<Informe> todos();
}
