package es.uclm.esi.iso2.servidor.estadisticas;

import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

/**
 * Informe operativo de un periodo para el personal de admisión (CU21): ocupación de las agendas, tiempos de
 * espera hasta la cita y cancelaciones.
 *
 * @param ocupacion        por modalidad, solo las que tienen agendas en el periodo
 * @param esperas          por prioridad (orden de atención), de las citas no canceladas del periodo
 * @param citasDelPeriodo  citas cuya fecha cae en el periodo, también las canceladas
 * @param cancelaciones    de ellas, las canceladas
 */
public record InformeOperativo(Periodo periodo, List<OcupacionModalidad> ocupacion, List<EsperaPrioridad> esperas,
        long citasDelPeriodo, long cancelaciones) {

    public InformeOperativo {
        ocupacion = List.copyOf(ocupacion);
        esperas = List.copyOf(esperas);
    }

    public long huecos() {
        return ocupacion.stream().mapToLong(OcupacionModalidad::huecos).sum();
    }

    public long huecosOcupados() {
        return ocupacion.stream().mapToLong(OcupacionModalidad::ocupados).sum();
    }

    public OptionalDouble ocupacionTotal() {
        return Porcentaje.de(huecosOcupados(), huecos());
    }

    public OptionalDouble tasaCancelacion() {
        return Porcentaje.de(cancelaciones, citasDelPeriodo);
    }

    @Override
    public String toString() {
        return "Informe operativo · " + periodo
                + "\n  Ocupación: " + Porcentaje.texto(ocupacionTotal(), " %") + " (" + huecosOcupados() + "/" + huecos()
                + " huecos)" + ocupacion.stream().map(o -> "\n    " + o).collect(Collectors.joining())
                + "\n  Espera hasta la cita:" + esperas.stream().map(e -> "\n    " + e).collect(Collectors.joining())
                + "\n  Cancelaciones: " + cancelaciones + " de " + citasDelPeriodo + " citas ("
                + Porcentaje.texto(tasaCancelacion(), " %") + ")";
    }
}
