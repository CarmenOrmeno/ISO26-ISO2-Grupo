package es.uclm.esi.iso2.servidor.estadisticas;

/** Contrato interno de los informes operativos (CU21). */
public interface ServicioInformesOperativos {

    InformeOperativo generar(Periodo periodo);
}
