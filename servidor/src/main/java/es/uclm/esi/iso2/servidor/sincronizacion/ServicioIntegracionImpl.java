package es.uclm.esi.iso2.servidor.interoperabilidad;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import es.uclm.esi.iso2.servidor.comun.ReglaNegocioException;
import es.uclm.esi.iso2.servidor.comun.Repositorio;
import es.uclm.esi.iso2.servidor.comun.Secuencia;
import es.uclm.esi.iso2.servidor.informes.Informe;
import es.uclm.esi.iso2.servidor.informes.ServicioInformes;
import es.uclm.esi.iso2.servidor.integracion.DocumentoHce;
import es.uclm.esi.iso2.servidor.integracion.PacienteAdmision;
import es.uclm.esi.iso2.servidor.integracion.SistemaAdmision;
import es.uclm.esi.iso2.servidor.integracion.SistemaExterno;
import es.uclm.esi.iso2.servidor.integracion.SistemaHce;
import es.uclm.esi.iso2.servidor.pacientes.DatosPaciente;
import es.uclm.esi.iso2.servidor.pacientes.ServicioPacientes;

public class ServicioIntegracionImpl implements ServicioIntegracion {

    private final List<SistemaExterno> sistemas;
    private final SistemaAdmision admision;
    private final SistemaHce hce;
    private final ServicioPacientes pacientes;
    private final ServicioInformes informes;
    private final Repositorio<Long, Long> informesEnviados;
    private final Repositorio<Sincronizacion, Long> historial;
    private final Clock reloj;
    private final Secuencia secuencia;

    /**
     * @param sistemas         todos los sistemas externos, para consultar su estado (abierto a añadir más)
     * @param informesEnviados ids de los informes ya publicados en la HCE
     */
    public ServicioIntegracionImpl(List<SistemaExterno> sistemas, SistemaAdmision admision, SistemaHce hce,
            ServicioPacientes pacientes, ServicioInformes informes, Repositorio<Long, Long> informesEnviados,
            Repositorio<Sincronizacion, Long> historial, Clock reloj) {
        this.sistemas = List.copyOf(sistemas);
        this.admision = admision;
        this.hce = hce;
        this.pacientes = pacientes;
        this.informes = informes;
        this.informesEnviados = informesEnviados;
        this.historial = historial;
        this.reloj = reloj;
        this.secuencia = Secuencia.continuando(historial, Sincronizacion::id);
    }

    @Override
    public List<EstadoSistema> estado() {
        return sistemas.stream().map(s -> new EstadoSistema(s.nombre(), s.disponible())).toList();
    }

    @Override
    public Sincronizacion importarPacientes() {
        List<PacienteAdmision> padron;
        try {
            padron = admision.pacientes();
        } catch (ReglaNegocioException e) {
            return guardar(TipoSincronizacion.IMPORTAR_PACIENTES, admision, 0, 0, 0, false, e.getMessage());
        }
        int importados = 0;
        int existentes = 0;
        List<String> rechazos = new ArrayList<>();
        for (PacienteAdmision p : padron) {
            try {
                if (pacientes.buscarPorDni(p.dni()).isPresent()) {
                    existentes++;
                } else {
                    pacientes.alta(new DatosPaciente(p.dni(), p.nombre(), p.apellidos(), p.fechaNacimiento(),
                            p.telefono(), p.email()));
                    importados++;
                }
            } catch (IllegalArgumentException | ReglaNegocioException e) {
                rechazos.add(p.dni() + ": " + e.getMessage());
            }
        }
        return guardar(TipoSincronizacion.IMPORTAR_PACIENTES, admision, importados, existentes, rechazos.size(),
                rechazos.isEmpty(), String.join("; ", rechazos));
    }

    @Override
    public Sincronizacion enviarInformesHce() {
        List<Informe> pendientes = informes.todos().stream().filter(i -> !informesEnviados.existe(i.id())).toList();
        int omitidos = informes.todos().size() - pendientes.size();
        int enviados = 0;
        for (Informe informe : pendientes) {
            try {
                hce.publicar(documento(informe));
            } catch (ReglaNegocioException e) {
                // los ya enviados quedan guardados; el resto se enviará en la siguiente sincronización
                return guardar(TipoSincronizacion.ENVIAR_INFORMES, hce, enviados, omitidos, 0, false,
                        e.getMessage() + " (" + (pendientes.size() - enviados) + " informes pendientes)");
            }
            informesEnviados.guardar(informe.id());
            enviados++;
        }
        return guardar(TipoSincronizacion.ENVIAR_INFORMES, hce, enviados, omitidos, 0, true, "");
    }

    @Override
    public List<Sincronizacion> sincronizaciones() {
        return historial.todos().stream().sorted(Comparator.comparing(Sincronizacion::id).reversed()).toList();
    }

    private DocumentoHce documento(Informe informe) {
        String dni = pacientes.obtener(informe.idPaciente()).getDni();
        return new DocumentoHce(dni, "informe " + informe.id(), "Informe radiológico de la solicitud "
                + informe.idSolicitud(), "Hallazgos: " + informe.hallazgos() + "\nConclusión: " + informe.conclusion(),
                informe.fecha());
    }

    private Sincronizacion guardar(TipoSincronizacion tipo, SistemaExterno sistema, int procesados, int omitidos,
            int rechazados, boolean correcta, String detalle) {
        Sincronizacion s = new Sincronizacion(secuencia.siguiente(), tipo, sistema.nombre(), LocalDateTime.now(reloj),
                procesados, omitidos, rechazados, correcta, detalle);
        secuencia.confirmar();
        return historial.guardar(s);
    }
}
