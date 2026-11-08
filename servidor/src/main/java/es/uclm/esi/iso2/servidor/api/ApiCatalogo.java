package es.uclm.esi.iso2.servidor.api;

import java.time.LocalTime;
import java.util.List;

import es.uclm.esi.iso2.servidor.catalogo.Modalidad;
import es.uclm.esi.iso2.servidor.catalogo.Prioridad;
import es.uclm.esi.iso2.servidor.catalogo.TipoPrueba;

/** CU03 · Configurar catálogo de tipos de prueba y parámetros del sistema (administrador). */
public interface ApiCatalogo {

    List<TipoPrueba> listar(String token);

    List<TipoPrueba> listarPorModalidad(String token, Modalidad modalidad);

    TipoPrueba obtener(String token, String codigo);

    TipoPrueba alta(String token, TipoPrueba tipo);

    TipoPrueba modificar(String token, TipoPrueba tipo);

    void baja(String token, String codigo);

    String parametros(String token);

    void cambiarHorario(String token, LocalTime apertura, LocalTime cierre);

    void cambiarPlazo(String token, Prioridad prioridad, int dias);

    void cambiarHorasRecordatorio(String token, int horas);
}
