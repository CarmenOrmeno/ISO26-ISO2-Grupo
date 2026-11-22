package es.uclm.esi.iso2.servidor.estadisticas;

import java.util.OptionalDouble;

import es.uclm.esi.iso2.servidor.catalogo.Modalidad;

/** Ocupación de las agendas de una modalidad en un periodo (CU21). */
public record OcupacionModalidad(Modalidad modalidad, long huecos, long ocupados) {

    public OptionalDouble porcentaje() {
        return Porcentaje.de(ocupados, huecos);
    }

    @Override
    public String toString() {
        return modalidad + ": " + ocupados + "/" + huecos + " huecos (" + Porcentaje.texto(porcentaje(), " %") + ")";
    }
}
