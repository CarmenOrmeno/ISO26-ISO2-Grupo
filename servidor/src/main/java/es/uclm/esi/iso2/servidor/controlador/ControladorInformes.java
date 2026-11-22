package es.uclm.esi.iso2.servidor.controlador;

import java.util.List;

import es.uclm.esi.iso2.servidor.api.ApiInformes;
import es.uclm.esi.iso2.servidor.comun.AccesoDenegadoException;
import es.uclm.esi.iso2.servidor.comun.NoEncontradoException;
import es.uclm.esi.iso2.servidor.informes.Informe;
import es.uclm.esi.iso2.servidor.informes.ServicioInformes;
import es.uclm.esi.iso2.servidor.integracion.ImagenDicom;
import es.uclm.esi.iso2.servidor.seguridad.ControlAcceso;
import es.uclm.esi.iso2.servidor.seguridad.Permiso;
import es.uclm.esi.iso2.servidor.solicitudes.ServicioSolicitudes;

/** El médico solo accede a informes e imágenes de pacientes a los que atiende (RNF-S3). */
public class ControladorInformes implements ApiInformes {

    private final ServicioInformes informes;
    private final ServicioSolicitudes solicitudes;
    private final ControlAcceso acceso;

    public ControladorInformes(ServicioInformes informes, ServicioSolicitudes solicitudes, ControlAcceso acceso) {
        this.informes = informes;
        this.solicitudes = solicitudes;
        this.acceso = acceso;
    }

    private void exigirPacienteSuyo(String medico, String idPaciente) {
        if (!informes.esPacienteDe(medico, idPaciente)) {
            throw new AccesoDenegadoException("El paciente " + idPaciente + " no es paciente de " + medico);
        }
    }

    @Override
    public Informe crearInforme(String token, long idSolicitud, String hallazgos, String conclusion) {
        String medico = acceso.exigir(token, Permiso.CREAR_INFORMES).login();
        exigirPacienteSuyo(medico, solicitudes.obtener(idSolicitud).getIdPaciente());
        return informes.crearInforme(idSolicitud, medico, hallazgos, conclusion);
    }

    @Override
    public Informe informeDeSolicitud(String token, long idSolicitud) {
        String medico = acceso.exigir(token, Permiso.CONSULTAR_INFORMES_PACIENTES).login();
        exigirPacienteSuyo(medico, solicitudes.obtener(idSolicitud).getIdPaciente());
        return informes.informeDeSolicitud(idSolicitud)
                .orElseThrow(() -> new NoEncontradoException("La solicitud " + idSolicitud + " no tiene informe"));
    }

    @Override
    public List<Informe> informesDePaciente(String token, String idPaciente) {
        String medico = acceso.exigir(token, Permiso.CONSULTAR_INFORMES_PACIENTES).login();
        exigirPacienteSuyo(medico, idPaciente);
        return informes.informesDePaciente(idPaciente);
    }

    @Override
    public List<ImagenDicom> imagenes(String token, long idSolicitud) {
        String medico = acceso.exigir(token, Permiso.CONSULTAR_INFORMES_PACIENTES).login();
        exigirPacienteSuyo(medico, solicitudes.obtener(idSolicitud).getIdPaciente());
        return informes.imagenes(idSolicitud);
    }
}
