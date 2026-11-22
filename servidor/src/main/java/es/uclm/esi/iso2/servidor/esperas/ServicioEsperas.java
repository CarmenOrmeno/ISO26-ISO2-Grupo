package es.uclm.esi.iso2.servidor.esperas;

import java.util.List;

import es.uclm.esi.iso2.servidor.catalogo.Modalidad;
import es.uclm.esi.iso2.servidor.catalogo.Prioridad;
import es.uclm.esi.iso2.servidor.citas.Cita;

/** Contrato interno de las listas de espera, prioridades y huecos (CU11). */
public interface ServicioEsperas {

    /** Solicitudes pendientes de cita, por prioridad y, a igual prioridad, de la más antigua a la más reciente. */
    List<EntradaEspera> listaDeEspera();

    List<EntradaEspera> listaDeEspera(Modalidad modalidad);

    /** Solo mientras la solicitud espera cita (SOLICITADA). */
    void cambiarPrioridad(long idSolicitud, Prioridad prioridad);

    /**
     * Cita la solicitud en el primer hueco libre futuro de su modalidad hasta su fecha límite (si ya está
     * fuera de plazo, dentro de un nuevo plazo de su prioridad desde hoy).
     */
    Cita citarEnPrimerHueco(long idSolicitud);

    /** Recorre la lista de espera de la modalidad en orden y cita cada solicitud que tenga hueco. */
    int rellenarHuecos(Modalidad modalidad);
}
