package es.uclm.esi.iso2.servidor.historial;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import es.uclm.esi.iso2.servidor.catalogo.ServicioCatalogo;
import es.uclm.esi.iso2.servidor.catalogo.TipoPrueba;
import es.uclm.esi.iso2.servidor.comun.AccesoDenegadoException;
import es.uclm.esi.iso2.servidor.comun.NoEncontradoException;
import es.uclm.esi.iso2.servidor.comun.Validaciones;
import es.uclm.esi.iso2.servidor.informes.Informe;
import es.uclm.esi.iso2.servidor.informes.ServicioInformes;
import es.uclm.esi.iso2.servidor.solicitudes.EstadoSolicitud;
import es.uclm.esi.iso2.servidor.solicitudes.ServicioSolicitudes;
import es.uclm.esi.iso2.servidor.solicitudes.SolicitudPrueba;

public class ServicioHistorialImpl implements ServicioHistorial {

    private final ServicioSolicitudes solicitudes;
    private final ServicioCatalogo catalogo;
    private final ServicioInformes informes;

    public ServicioHistorialImpl(ServicioSolicitudes solicitudes, ServicioCatalogo catalogo,
            ServicioInformes informes) {
        this.solicitudes = solicitudes;
        this.catalogo = catalogo;
        this.informes = informes;
    }

    @Override
    public List<EntradaHistorial> historial(String idPaciente) {
        return solicitudes.porPaciente(idPaciente).stream()
                .filter(ServicioHistorialImpl::realizada)
                .map(this::entrada)
                .sorted(Comparator.comparing(EntradaHistorial::fechaRealizacion).reversed())
                .toList();
    }

    @Override
    public List<EntradaHistorial> historial(String idPaciente, LocalDate desde, LocalDate hasta) {
        Validaciones.obligatorio(desde, "desde");
        Validaciones.obligatorio(hasta, "hasta");
        if (hasta.isBefore(desde)) {
            throw new IllegalArgumentException("La fecha final no puede ser anterior a la inicial");
        }
        return historial(idPaciente).stream().filter(e -> {
            LocalDate dia = e.fechaRealizacion().toLocalDate();
            return !dia.isBefore(desde) && !dia.isAfter(hasta);
        }).toList();
    }

    @Override
    public Informe informe(String idPaciente, long idSolicitud) {
        SolicitudPrueba s = solicitudes.obtener(idSolicitud);
        if (!s.getIdPaciente().equals(idPaciente)) {
            throw new AccesoDenegadoException("La prueba " + idSolicitud + " no es suya");
        }
        return informes.informeDeSolicitud(idSolicitud)
                .orElseThrow(() -> new NoEncontradoException("La prueba " + idSolicitud + " aún no tiene informe"));
    }

    private static boolean realizada(SolicitudPrueba s) {
        return s.getEstado() == EstadoSolicitud.REALIZADA || s.getEstado() == EstadoSolicitud.INFORMADA;
    }

    private EntradaHistorial entrada(SolicitudPrueba s) {
        TipoPrueba tipo = catalogo.obtener(s.getCodigoTipo());
        return new EntradaHistorial(s.getId(), tipo.codigo(), tipo.nombre(), tipo.modalidad(),
                s.getFechaRealizacion(), s.getEstado(), s.getEstado() == EstadoSolicitud.INFORMADA);
    }
}
