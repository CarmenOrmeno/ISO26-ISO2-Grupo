package es.uclm.esi.iso2.servidor.persistencia;

import java.io.Serializable;
import java.util.function.Function;

import es.uclm.esi.iso2.servidor.comun.Repositorio;

/** Repositorios en memoria: pruebas unitarias y ejecución sin base de datos (los datos no se conservan). */
public class FabricaEnMemoria implements FabricaRepositorios {

    @Override
    public <T extends Serializable, ID> Repositorio<T, ID> crear(String nombre, Function<T, ID> extractorId) {
        return new RepositorioEnMemoria<>(extractorId);
    }

    @Override
    public String descripcion() {
        return "memoria (los datos se pierden al cerrar)";
    }
}
