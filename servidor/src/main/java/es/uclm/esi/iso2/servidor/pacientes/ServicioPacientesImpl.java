package es.uclm.esi.iso2.servidor.pacientes;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import es.uclm.esi.iso2.servidor.comun.ReglaNegocioException;
import es.uclm.esi.iso2.servidor.comun.Repositorio;

public class ServicioPacientesImpl implements ServicioPacientes {

    private final Repositorio<Paciente, String> repositorio;
    private final Clock reloj;
    private long secuencia;

    public ServicioPacientesImpl(Repositorio<Paciente, String> repositorio, Clock reloj) {
        this.repositorio = repositorio;
        this.reloj = reloj;
    }

    @Override
    public Paciente alta(DatosPaciente datos) {
        Paciente paciente = new Paciente("PAC-" + (secuencia + 1), datos, LocalDate.now(reloj));
        if (buscarPorDni(paciente.getDni()).isPresent()) {
            throw new ReglaNegocioException("Ya existe un paciente con DNI " + paciente.getDni());
        }
        secuencia++;
        return repositorio.guardar(paciente);
    }

    @Override
    public Paciente modificar(String id, DatosPaciente datos) {
        Paciente paciente = repositorio.obtener(id);
        paciente.modificar(datos, LocalDate.now(reloj));
        return repositorio.guardar(paciente);
    }

    @Override
    public void baja(String id) {
        Paciente paciente = repositorio.obtener(id);
        paciente.darDeBaja();
        repositorio.guardar(paciente);
    }

    @Override
    public Paciente obtener(String id) {
        return repositorio.obtener(id);
    }

    @Override
    public Optional<Paciente> buscarPorDni(String dni) {
        String normalizado = Dni.normalizar(dni);
        return repositorio.filtrar(p -> p.getDni().equals(normalizado)).stream().findFirst();
    }

    @Override
    public List<Paciente> activos() {
        return repositorio.filtrar(Paciente::isActivo);
    }

    @Override
    public Paciente obtenerActivo(String id) {
        Paciente paciente = repositorio.buscar(id)
                .orElseThrow(() -> new ReglaNegocioException("No existe el paciente " + id));
        if (!paciente.isActivo()) {
            throw new ReglaNegocioException("El paciente " + id + " está dado de baja");
        }
        return paciente;
    }
}
