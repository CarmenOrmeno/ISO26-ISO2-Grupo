package es.uclm.esi.iso2.servidor.persistencia;

import java.util.Optional;
import java.util.function.UnaryOperator;

import es.uclm.esi.iso2.servidor.comun.Validaciones;

/** Datos de conexión JDBC a MySQL. La contraseña nunca se muestra en {@link #toString()}. */
public record ConfiguracionBd(String url, String usuario, String clave) {

    public ConfiguracionBd {
        url = Validaciones.texto(url, "url");
        if (!url.startsWith("jdbc:mysql://")) {
            throw new IllegalArgumentException("La URL debe ser jdbc:mysql://… (es " + url + ")");
        }
        usuario = Validaciones.texto(usuario, "usuario");
        clave = clave == null ? "" : clave;
    }

    /** Lee la configuración de variables de entorno (o propiedades del sistema con el mismo nombre). */
    public static Optional<ConfiguracionBd> desdeEntorno(String variableUrl) {
        return desde(variableUrl, nombre -> Optional.ofNullable(System.getProperty(nombre))
                .orElseGet(() -> System.getenv(nombre)));
    }

    static Optional<ConfiguracionBd> desde(String variableUrl, UnaryOperator<String> entorno) {
        String url = entorno.apply(variableUrl);
        if (url == null || url.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(new ConfiguracionBd(url, entorno.apply("RADIO_DB_USUARIO"),
                entorno.apply("RADIO_DB_CLAVE")));
    }

    @Override
    public String toString() {
        return url + " (usuario " + usuario + ")";
    }
}
