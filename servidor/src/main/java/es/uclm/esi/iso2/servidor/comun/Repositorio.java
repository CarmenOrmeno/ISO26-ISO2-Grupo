package es.uclm.esi.iso2.servidor.comun;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Contrato de persistencia de un componente (D-008). Los servicios dependen de esta interfaz,
 * nunca de su implementación.
 *
 * @param <T>  tipo de la entidad
 * @param <ID> tipo del identificador
 */
public interface Repositorio<T, ID> {

    T guardar(T entidad);

    Optional<T> buscar(ID id);

    /** Devuelve la entidad o lanza {@link NoEncontradoException}. */
    default T obtener(ID id) {
        return buscar(id).orElseThrow(() -> new NoEncontradoException("No existe el elemento " + id));
    }

    List<T> todos();

    default List<T> filtrar(Predicate<T> condicion) {
        return todos().stream().filter(condicion).toList();
    }

    boolean existe(ID id);

    void eliminar(ID id);
}
