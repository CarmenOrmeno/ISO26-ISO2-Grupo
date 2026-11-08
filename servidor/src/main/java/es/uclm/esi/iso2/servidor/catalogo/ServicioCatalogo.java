package es.uclm.esi.iso2.servidor.catalogo;

import java.util.List;

/** Contrato interno del catálogo de tipos de prueba y de los parámetros del sistema (CU03). */
public interface ServicioCatalogo {

    TipoPrueba alta(TipoPrueba tipo);

    TipoPrueba modificar(TipoPrueba tipo);

    void baja(String codigo);

    TipoPrueba obtener(String codigo);

    List<TipoPrueba> listar();

    List<TipoPrueba> listarPorModalidad(Modalidad modalidad);

    ParametrosSistema parametros();
}
