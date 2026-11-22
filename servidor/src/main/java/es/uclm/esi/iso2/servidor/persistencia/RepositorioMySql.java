package es.uclm.esi.iso2.servidor.persistencia;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import es.uclm.esi.iso2.servidor.comun.NoEncontradoException;
import es.uclm.esi.iso2.servidor.comun.Repositorio;

/**
 * {@link Repositorio} en MySQL. Cada entidad (agregado) se guarda serializada en una fila de su tabla,
 * identificada por su id; el orden de inserción se conserva como en {@link
 * es.uclm.esi.iso2.servidor.persistencia.RepositorioEnMemoria}. Al leer solo se admiten clases del servidor y del
 * JDK (filtro de deserialización).
 */
public class RepositorioMySql<T extends Serializable, ID> implements Repositorio<T, ID> {

    /** Fuente de conexiones JDBC (en producción, {@link FabricaMySql#conectar()}). */
    @FunctionalInterface
    public interface Conexiones {
        Connection abrir() throws SQLException;
    }

    private static final ObjectInputFilter CLASES_PERMITIDAS = ObjectInputFilter.Config
            .createFilter("es.uclm.esi.iso2.servidor.**;java.**;maxdepth=20;!*");

    private final String tabla;
    private final Function<T, ID> extractorId;
    private final Conexiones conexiones;

    public RepositorioMySql(String tabla, Function<T, ID> extractorId, Conexiones conexiones) {
        if (tabla == null || !tabla.matches("[a-z][a-z_]{0,63}")) {
            throw new IllegalArgumentException("Nombre de tabla no válido: " + tabla);
        }
        this.tabla = tabla;
        this.extractorId = Objects.requireNonNull(extractorId);
        this.conexiones = Objects.requireNonNull(conexiones);
        ejecutar("CREATE TABLE IF NOT EXISTS " + tabla + " (orden BIGINT AUTO_INCREMENT PRIMARY KEY, "
                + "id VARCHAR(191) NOT NULL UNIQUE, datos LONGBLOB NOT NULL)");
    }

    @Override
    public T guardar(T entidad) {
        Objects.requireNonNull(entidad, "La entidad no puede ser nula");
        String sql = "INSERT INTO " + tabla + " (id, datos) VALUES (?, ?) AS nuevo "
                + "ON DUPLICATE KEY UPDATE datos = nuevo.datos";
        try (Connection c = conexiones.abrir(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, clave(extractorId.apply(entidad)));
            ps.setBytes(2, serializar(entidad));
            ps.executeUpdate();
            return entidad;
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo guardar en " + tabla, e);
        }
    }

    @Override
    public Optional<T> buscar(ID id) {
        List<T> encontradas = consultar("SELECT datos FROM " + tabla + " WHERE id = ?", clave(id));
        return encontradas.stream().findFirst();
    }

    @Override
    public List<T> todos() {
        return consultar("SELECT datos FROM " + tabla + " ORDER BY orden", null);
    }

    @Override
    public boolean existe(ID id) {
        return buscar(id).isPresent();
    }

    @Override
    public void eliminar(ID id) {
        try (Connection c = conexiones.abrir();
                PreparedStatement ps = c.prepareStatement("DELETE FROM " + tabla + " WHERE id = ?")) {
            ps.setString(1, clave(id));
            if (ps.executeUpdate() == 0) {
                throw new NoEncontradoException("No existe el elemento " + id);
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo eliminar de " + tabla, e);
        }
    }

    private List<T> consultar(String sql, String parametro) {
        try (Connection c = conexiones.abrir(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (parametro != null) {
                ps.setString(1, parametro);
            }
            List<T> resultado = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(deserializar(rs.getBytes(1)));
                }
            }
            return resultado;
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo leer de " + tabla, e);
        }
    }

    private void ejecutar(String sql) {
        try (Connection c = conexiones.abrir(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.execute();
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo preparar la tabla " + tabla, e);
        }
    }

    private static String clave(Object id) {
        return String.valueOf(Objects.requireNonNull(id, "El id no puede ser nulo"));
    }

    private static byte[] serializar(Serializable entidad) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream salida = new ObjectOutputStream(bytes)) {
            salida.writeObject(entidad);
        } catch (IOException e) {
            throw new PersistenciaException("No se pudo serializar " + entidad.getClass().getSimpleName(), e);
        }
        return bytes.toByteArray();
    }

    @SuppressWarnings("unchecked")
    private T deserializar(byte[] datos) {
        try (ObjectInputStream entrada = new ObjectInputStream(new ByteArrayInputStream(datos))) {
            entrada.setObjectInputFilter(CLASES_PERMITIDAS);
            return (T) entrada.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new PersistenciaException("Datos no válidos en " + tabla, e);
        }
    }
}
