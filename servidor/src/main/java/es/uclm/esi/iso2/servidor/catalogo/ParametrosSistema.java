package es.uclm.esi.iso2.servidor.catalogo;

import java.time.LocalTime;
import java.util.EnumMap;
import java.util.Map;

import es.uclm.esi.iso2.servidor.comun.Validaciones;

/** Parámetros configurables del servicio: horario, políticas de citación y recordatorios (CU03). */
public class ParametrosSistema {

    private LocalTime apertura = LocalTime.of(8, 0);
    private LocalTime cierre = LocalTime.of(21, 0);
    private final Map<Prioridad, Integer> plazoMaximoDias = new EnumMap<>(Map.of(
            Prioridad.URGENTE, 1, Prioridad.PREFERENTE, 15, Prioridad.ORDINARIO, 90));
    private int horasAntelacionRecordatorio = 24;

    public void cambiarHorario(LocalTime nuevaApertura, LocalTime nuevoCierre) {
        Validaciones.obligatorio(nuevaApertura, "apertura");
        Validaciones.obligatorio(nuevoCierre, "cierre");
        if (!nuevaApertura.isBefore(nuevoCierre)) {
            throw new IllegalArgumentException("La apertura debe ser anterior al cierre");
        }
        apertura = nuevaApertura;
        cierre = nuevoCierre;
    }

    /**
     * Cambia el plazo máximo de espera de una prioridad. Debe mantenerse el orden
     * URGENTE ≤ PREFERENTE ≤ ORDINARIO y ser de al menos 1 día.
     */
    public void cambiarPlazo(Prioridad prioridad, int dias) {
        Validaciones.obligatorio(prioridad, "prioridad");
        Validaciones.rango(dias, 1, 365, "plazo (días)");
        Map<Prioridad, Integer> propuesta = new EnumMap<>(plazoMaximoDias);
        propuesta.put(prioridad, dias);
        if (propuesta.get(Prioridad.URGENTE) > propuesta.get(Prioridad.PREFERENTE)
                || propuesta.get(Prioridad.PREFERENTE) > propuesta.get(Prioridad.ORDINARIO)) {
            throw new IllegalArgumentException("Los plazos deben cumplir URGENTE ≤ PREFERENTE ≤ ORDINARIO");
        }
        plazoMaximoDias.put(prioridad, dias);
    }

    public void cambiarHorasRecordatorio(int horas) {
        horasAntelacionRecordatorio = Validaciones.rango(horas, 1, 168, "antelación del recordatorio (h)");
    }

    /** Indica si el intervalo [inicio, fin] cabe en el horario del servicio. */
    public boolean dentroDeHorario(LocalTime inicio, LocalTime fin) {
        return !inicio.isBefore(apertura) && !fin.isAfter(cierre) && inicio.isBefore(fin);
    }

    public LocalTime apertura() {
        return apertura;
    }

    public LocalTime cierre() {
        return cierre;
    }

    public int plazoMaximoDias(Prioridad prioridad) {
        return plazoMaximoDias.get(prioridad);
    }

    public int horasAntelacionRecordatorio() {
        return horasAntelacionRecordatorio;
    }

    @Override
    public String toString() {
        return "Horario " + apertura + "-" + cierre + " · plazos (días) " + plazoMaximoDias
                + " · recordatorio " + horasAntelacionRecordatorio + " h antes";
    }
}
