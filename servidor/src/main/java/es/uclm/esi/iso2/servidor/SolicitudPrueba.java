package es.uclm.esi.iso2.servidor;

public class SolicitudPrueba {
    public enum Estado {
        PENDIENTE,
        REALIZADA
    }

    private final int prioridad;
    private Estado estado = Estado.PENDIENTE;
    private Object informe;

    public SolicitudPrueba(int prioridad) {
        this.prioridad = prioridad;
    }

    public Object getInforme() {
        return informe;
    }

    public int getPrioridad() {
        return prioridad;
    }

    public Estado getEstado() {
        return estado;
    }

    public void registrarRealizacion() {
        estado = Estado.REALIZADA;
    }

    public static void main(String[] args) {
        System.out.println("Solicitud de prueba ejecutada");
    }
}
