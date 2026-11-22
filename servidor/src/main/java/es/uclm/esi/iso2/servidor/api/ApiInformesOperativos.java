package es.uclm.esi.iso2.servidor.api;

import java.time.LocalDate;

import es.uclm.esi.iso2.servidor.estadisticas.InformeOperativo;

/** CU21 · Generar informes operativos: ocupación, tiempos de espera y cancelaciones (administrativo). */
public interface ApiInformesOperativos {

    /** Informe del periodo entre dos fechas (ambas incluidas). */
    InformeOperativo generar(String token, LocalDate desde, LocalDate hasta);
}
