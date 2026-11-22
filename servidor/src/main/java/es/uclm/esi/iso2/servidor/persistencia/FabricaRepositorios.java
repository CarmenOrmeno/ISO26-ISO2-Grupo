package es.uclm.esi.iso2.servidor.persistencia;

import java.io.Serializable;
import java.util.function.Function;

import es.uclm.esi.iso2.servidor.comun.Repositorio;

/**
 * Crea los repositorios de los componentes (D-008, D-018). La raíz de composición elige la implementación
 * (memoria o MySQL) y los servicios solo ven {@link Repositorio} (inversión de dependencias).
 */
public interface FabricaRepositorios {

    /**
     * @param nombre      nombre lógico del almacén (en MySQL, la tabla): minúsculas y guiones bajos
     * @param extractorId cómo obtener el identificador de una entidad
     */
    <T extends Serializable, ID> Repositorio<T, ID> crear(String nombre, Function<T, ID> extractorId);

    /** Descripción para mostrar al usuario (p. ej. «MySQL jdbc:mysql://…»). */
    String descripcion();

    /** MySQL si está configurado en el entorno (RADIO_DB_URL…); si no, memoria. */
    static FabricaRepositorios segunEntorno() {
        return ConfiguracionBd.desdeEntorno("RADIO_DB_URL").<FabricaRepositorios>map(FabricaMySql::new)
                .orElseGet(FabricaEnMemoria::new);
    }
}
