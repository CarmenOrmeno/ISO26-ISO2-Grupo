package es.uclm.esi.iso2.servidor.catalogo;

/** Modalidades de imagen (códigos DICOM). */
public enum Modalidad {
    RX("Radiografía convencional"),
    US("Ecografía"),
    MG("Mamografía y tomosíntesis"),
    TC("Tomografía computarizada"),
    RM("Resonancia magnética");

    private final String descripcion;

    Modalidad(String descripcion) {
        this.descripcion = descripcion;
    }

    public String descripcion() {
        return descripcion;
    }
}
