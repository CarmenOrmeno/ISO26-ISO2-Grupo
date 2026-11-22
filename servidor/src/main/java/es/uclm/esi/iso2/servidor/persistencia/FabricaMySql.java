package es.uclm.esi.iso2.servidor.persistencia;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.function.Function;

import es.uclm.esi.iso2.servidor.comun.Repositorio;

/** Repositorios en MySQL (D-018): una tabla por almacén en la base de datos configurada. */
public class FabricaMySql implements FabricaRepositorios {

    private final ConfiguracionBd configuracion;

    public FabricaMySql(ConfiguracionBd configuracion) {
        this.configuracion = configuracion;
    }

    @Override
    public <T extends Serializable, ID> Repositorio<T, ID> crear(String nombre, Function<T, ID> extractorId) {
        return new RepositorioMySql<>(nombre, extractorId, this::conectar);
    }

    @Override
    public String descripcion() {
        return "MySQL " + configuracion;
    }

    Connection conectar() throws SQLException {
        return DriverManager.getConnection(configuracion.url(), configuracion.usuario(), configuracion.clave());
    }
}
