package es.uclm.esi.iso2.servidor.comun;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/** Implementación en memoria de {@link Repositorio}; conserva el orden de inserción. */
public class RepositorioEnMemoria<T, ID> implements Repositorio<T, ID> {

    private final Map<ID, T> datos = new LinkedHashMap<>();
    private final Function<T, ID> extractorId;

    public RepositorioEnMemoria(Function<T, ID> extractorId) {
        this.extractorId = Objects.requireNonNull(extractorId);
    }

    @Override
    public T guardar(T entidad) {
        Objects.requireNonNull(entidad, "La entidad no puede ser nula");
        datos.put(extractorId.apply(entidad), entidad);
        return entidad;
    }

    @Override
    public Optional<T> buscar(ID id) {
        return Optional.ofNullable(datos.get(id));
    }

    @Override
    public List<T> todos() {
        return new ArrayList<>(datos.values());
    }

    @Override
    public boolean existe(ID id) {
        return datos.containsKey(id);
    }

    @Override
    public void eliminar(ID id) {
        if (datos.remove(id) == null) {
            throw new NoEncontradoException("No existe el elemento " + id);
        }
    }
}
