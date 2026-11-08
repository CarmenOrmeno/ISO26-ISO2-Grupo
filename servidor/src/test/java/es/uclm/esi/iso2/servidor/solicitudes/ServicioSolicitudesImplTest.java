package es.uclm.esi.iso2.servidor.solicitudes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.uclm.esi.iso2.servidor.catalogo.Modalidad;
import es.uclm.esi.iso2.servidor.catalogo.ParametrosSistema;
import es.uclm.esi.iso2.servidor.catalogo.Prioridad;
import es.uclm.esi.iso2.servidor.catalogo.ServicioCatalogoImpl;
import es.uclm.esi.iso2.servidor.catalogo.TipoPrueba;
import es.uclm.esi.iso2.servidor.comun.AccesoDenegadoException;
import es.uclm.esi.iso2.servidor.comun.NoEncontradoException;
import es.uclm.esi.iso2.servidor.comun.ReglaNegocioException;
import es.uclm.esi.iso2.servidor.comun.RelojAjustable;
import es.uclm.esi.iso2.servidor.comun.RepositorioEnMemoria;
import es.uclm.esi.iso2.servidor.pacientes.DatosPaciente;
import es.uclm.esi.iso2.servidor.pacientes.Paciente;
import es.uclm.esi.iso2.servidor.pacientes.ServicioPacientes;
import es.uclm.esi.iso2.servidor.pacientes.ServicioPacientesImpl;

class ServicioSolicitudesImplTest {

    private ServicioPacientes pacientes;
    private ServicioSolicitudes servicio;

    @BeforeEach
    void preparar() {
        RelojAjustable reloj = new RelojAjustable(LocalDateTime.of(2026, 11, 2, 9, 0));
        pacientes = new ServicioPacientesImpl(new RepositorioEnMemoria<>(Paciente::getId), reloj);
        pacientes.alta(new DatosPaciente("12345678Z", "L", "M", LocalDate.of(1980, 1, 1), "600000000", null));
        pacientes.alta(new DatosPaciente("87654321X", "A", "G", LocalDate.of(1960, 1, 1), "600000001", null));
        ServicioCatalogoImpl catalogo = new ServicioCatalogoImpl(new RepositorioEnMemoria<>(TipoPrueba::codigo),
                new ParametrosSistema());
        catalogo.alta(new TipoPrueba("RX-T", "Rx", Modalidad.RX, "RX1", 10, "", false, false, "", false));
        catalogo.alta(new TipoPrueba("US-A", "Eco", Modalidad.US, "E1", 20, "", false, false, "", true));
        servicio = new ServicioSolicitudesImpl(new RepositorioEnMemoria<>(SolicitudPrueba::getId), pacientes,
                catalogo, reloj);
    }

    private static DatosSolicitud datos(String paciente, String tipo) {
        return new DatosSolicitud(paciente, tipo, "Indicación", "Justificación", Prioridad.ORDINARIO);
    }

    @Test
    @DisplayName("CU04: solicitar asigna id correlativo, estado SOLICITADA y fecha del reloj")
    void solicitar() {
        SolicitudPrueba s = servicio.solicitar("medico1", datos("PAC-1", "RX-T"));
        assertEquals(1, s.getId());
        assertEquals(LocalDateTime.of(2026, 11, 2, 9, 0), s.getFechaSolicitud());
        assertEquals(2, servicio.solicitar("medico1", datos("PAC-1", "US-A")).getId());
        assertEquals(2, servicio.pendientes().size());
        assertEquals(2, servicio.porPaciente("PAC-1").size());
        assertEquals(2, servicio.porMedico("medico1").size());
        assertEquals(0, servicio.porMedico("medico2").size());
        assertEquals(2, servicio.todas().size());
    }

    @Test
    @DisplayName("Paciente inexistente o de baja, o tipo inexistente: no se crea ni consume id")
    void errores() {
        assertThrows(ReglaNegocioException.class, () -> servicio.solicitar("m", datos("PAC-9", "RX-T")));
        pacientes.baja("PAC-2");
        assertThrows(ReglaNegocioException.class, () -> servicio.solicitar("m", datos("PAC-2", "RX-T")));
        assertThrows(NoEncontradoException.class, () -> servicio.solicitar("m", datos("PAC-1", "XX")));
        assertEquals(1, servicio.solicitar("m", datos("PAC-1", "RX-T")).getId());
    }

    @Test
    @DisplayName("Una solicitud abierta del mismo tipo para el mismo paciente es un duplicado; cancelada ya no")
    void duplicados() {
        SolicitudPrueba s = servicio.solicitar("medico1", datos("PAC-1", "RX-T"));
        assertThrows(ReglaNegocioException.class, () -> servicio.solicitar("medico2", datos("PAC-1", "RX-T")));
        servicio.solicitar("medico1", datos("PAC-2", "RX-T"));
        servicio.cancelar(s.getId(), "medico1", "Error de petición");
        assertEquals(EstadoSolicitud.CANCELADA, servicio.obtener(s.getId()).getEstado());
        servicio.solicitar("medico2", datos("PAC-1", "RX-T"));
    }

    @Test
    @DisplayName("Solo el médico solicitante puede cancelar")
    void cancelarOtroMedico() {
        SolicitudPrueba s = servicio.solicitar("medico1", datos("PAC-1", "RX-T"));
        assertThrows(AccesoDenegadoException.class, () -> servicio.cancelar(s.getId(), "medico2", "x"));
        assertThrows(NoEncontradoException.class, () -> servicio.cancelar(99, "medico1", "x"));
    }
}
