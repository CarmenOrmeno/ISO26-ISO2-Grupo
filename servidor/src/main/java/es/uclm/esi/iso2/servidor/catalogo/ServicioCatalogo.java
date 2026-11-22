package es.uclm.esi.iso2.servidor.catalogo;

import java.time.LocalTime;
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

    /** CU03: horario del servicio (se guarda). */
    void cambiarHorario(LocalTime apertura, LocalTime cierre);

    /** CU03: plazo máximo de espera de una prioridad (se guarda). */
    void cambiarPlazo(Prioridad prioridad, int dias);

    /** CU03: antelación de los recordatorios (se guarda). */
    void cambiarHorasRecordatorio(int horas);
}
