package es.uclm.esi.iso2.servidor.integracion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import es.uclm.esi.iso2.servidor.comun.ReglaNegocioException;

class PacsSimuladoTest {

    private final PacsSimulado pacs = new PacsSimulado();

    @Test
    @DisplayName("Registrar un estudio devuelve su UID y sus imágenes")
    void registrar() {
        String uid = pacs.registrarEstudio(7, "PAC-1", "RX", 2);
        assertEquals(PacsSimulado.RAIZ_UID + ".7", uid);
        assertEquals(2, pacs.imagenes(7).get(0).numeroImagenes());
        assertTrue(pacs.imagenes(8).isEmpty());
    }

    @ParameterizedTest(name = "{0} imágenes → inválido")
    @ValueSource(ints = { 0, -1 })
    @DisplayName("Valor límite: al menos 1 imagen")
    void sinImagenes(int n) {
        assertThrows(IllegalArgumentException.class, () -> pacs.registrarEstudio(1, "PAC-1", "RX", n));
        pacs.registrarEstudio(1, "PAC-1", "RX", 1);
    }

    @Test
    @DisplayName("PACS caído: registrar y consultar fallan")
    void caido() {
        pacs.setDisponible(false);
        assertEquals(false, pacs.disponible());
        assertThrows(ReglaNegocioException.class, () -> pacs.registrarEstudio(1, "PAC-1", "RX", 1));
        assertThrows(ReglaNegocioException.class, () -> pacs.imagenes(1));
    }
}
