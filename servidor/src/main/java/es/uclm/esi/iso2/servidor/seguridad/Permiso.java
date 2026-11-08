package es.uclm.esi.iso2.servidor.seguridad;

/**
 * Operaciones protegidas de la API (D-011). {@code datosClinicos} indica si la operación accede a
 * datos clínicos y, por tanto, debe auditarse con especial atención (RNF-S2).
 */
public enum Permiso {
    // CU01
    CONSULTAR_PERFIL(false);

    private final boolean datosClinicos;

    Permiso(boolean datosClinicos) {
        this.datosClinicos = datosClinicos;
    }

    public boolean accedeADatosClinicos() {
        return datosClinicos;
    }
}
