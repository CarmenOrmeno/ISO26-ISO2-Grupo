package es.uclm.esi.iso2.servidor.pacientes;

import java.time.LocalDate;
import java.util.regex.Pattern;

import es.uclm.esi.iso2.servidor.comun.ReglaNegocioException;
import es.uclm.esi.iso2.servidor.comun.Validaciones;

/** Paciente del SESCAM. La baja es lógica: se conserva para el historial. */
public class Paciente {

    /** Fecha de nacimiento más antigua admitida. */
    public static final LocalDate NACIMIENTO_MINIMO = LocalDate.of(1900, 1, 1);
    private static final Pattern TELEFONO = Pattern.compile("[6789]\\d{8}");
    private static final Pattern EMAIL = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");

    private final String id;
    private final String dni;
    private String nombre;
    private String apellidos;
    private LocalDate fechaNacimiento;
    private String telefono;
    private String email;
    private boolean activo = true;

    public Paciente(String id, DatosPaciente datos, LocalDate hoy) {
        this.id = Validaciones.texto(id, "id");
        Validaciones.obligatorio(datos, "datos");
        this.dni = Dni.normalizar(datos.dni());
        aplicar(datos, hoy);
    }

    /** Modifica los datos. El DNI identifica al paciente y no se puede cambiar. */
    public void modificar(DatosPaciente datos, LocalDate hoy) {
        Validaciones.obligatorio(datos, "datos");
        if (!dni.equals(Dni.normalizar(datos.dni()))) {
            throw new ReglaNegocioException("El DNI de un paciente no se puede modificar");
        }
        if (!activo) {
            throw new ReglaNegocioException("No se puede modificar un paciente dado de baja");
        }
        aplicar(datos, hoy);
    }

    private void aplicar(DatosPaciente datos, LocalDate hoy) {
        String nuevoNombre = Validaciones.texto(datos.nombre(), "nombre");
        String nuevosApellidos = Validaciones.texto(datos.apellidos(), "apellidos");
        LocalDate nacimiento = Validaciones.obligatorio(datos.fechaNacimiento(), "fecha de nacimiento");
        if (nacimiento.isAfter(hoy) || nacimiento.isBefore(NACIMIENTO_MINIMO)) {
            throw new IllegalArgumentException("Fecha de nacimiento fuera de rango: " + nacimiento);
        }
        String tel = Validaciones.texto(datos.telefono(), "teléfono");
        if (!TELEFONO.matcher(tel).matches()) {
            throw new IllegalArgumentException("El teléfono debe tener 9 dígitos y empezar por 6, 7, 8 o 9");
        }
        String correo = datos.email() == null || datos.email().isBlank() ? null : datos.email().strip();
        if (correo != null && !EMAIL.matcher(correo).matches()) {
            throw new IllegalArgumentException("El correo electrónico no es válido: " + correo);
        }
        this.nombre = nuevoNombre;
        this.apellidos = nuevosApellidos;
        this.fechaNacimiento = nacimiento;
        this.telefono = tel;
        this.email = correo;
    }

    public void darDeBaja() {
        if (!activo) {
            throw new ReglaNegocioException("El paciente " + id + " ya estaba dado de baja");
        }
        activo = false;
    }

    public String getId() {
        return id;
    }

    public String getDni() {
        return dni;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public String getNombreCompleto() {
        return nombre + " " + apellidos;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getEmail() {
        return email;
    }

    public boolean isActivo() {
        return activo;
    }

    @Override
    public String toString() {
        return id + " · " + getNombreCompleto() + " · DNI " + dni + " · tel. " + telefono
                + (activo ? "" : " · BAJA");
    }
}
