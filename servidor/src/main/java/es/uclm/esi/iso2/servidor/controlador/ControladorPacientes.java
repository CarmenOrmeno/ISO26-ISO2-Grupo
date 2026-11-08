package es.uclm.esi.iso2.servidor.controlador;

import java.util.List;

import es.uclm.esi.iso2.servidor.api.ApiPacientes;
import es.uclm.esi.iso2.servidor.comun.NoEncontradoException;
import es.uclm.esi.iso2.servidor.pacientes.DatosPaciente;
import es.uclm.esi.iso2.servidor.pacientes.Paciente;
import es.uclm.esi.iso2.servidor.pacientes.ServicioPacientes;
import es.uclm.esi.iso2.servidor.seguridad.ControlAcceso;
import es.uclm.esi.iso2.servidor.seguridad.Permiso;

public class ControladorPacientes implements ApiPacientes {

    private final ServicioPacientes servicio;
    private final ControlAcceso acceso;

    public ControladorPacientes(ServicioPacientes servicio, ControlAcceso acceso) {
        this.servicio = servicio;
        this.acceso = acceso;
    }

    @Override
    public Paciente alta(String token, DatosPaciente datos) {
        acceso.exigir(token, Permiso.GESTIONAR_PACIENTES);
        return servicio.alta(datos);
    }

    @Override
    public Paciente modificar(String token, String idPaciente, DatosPaciente datos) {
        acceso.exigir(token, Permiso.GESTIONAR_PACIENTES);
        return servicio.modificar(idPaciente, datos);
    }

    @Override
    public void baja(String token, String idPaciente) {
        acceso.exigir(token, Permiso.GESTIONAR_PACIENTES);
        servicio.baja(idPaciente);
    }

    @Override
    public Paciente obtener(String token, String idPaciente) {
        acceso.exigir(token, Permiso.CONSULTAR_PACIENTES);
        return servicio.obtener(idPaciente);
    }

    @Override
    public Paciente buscarPorDni(String token, String dni) {
        acceso.exigir(token, Permiso.CONSULTAR_PACIENTES);
        return servicio.buscarPorDni(dni)
                .orElseThrow(() -> new NoEncontradoException("No hay ningún paciente con DNI " + dni));
    }

    @Override
    public List<Paciente> listarActivos(String token) {
        acceso.exigir(token, Permiso.CONSULTAR_PACIENTES);
        return servicio.activos();
    }
}
