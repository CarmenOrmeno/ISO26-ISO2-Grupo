package es.uclm.esi.iso2.servidor.integracion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.uclm.esi.iso2.servidor.comun.ReglaNegocioException;

/** CU19 · Los simulados de HCE y Admisión cumplen su contrato (LSP), también cuando están caídos. */
class SistemasSimuladosTest {

    private static final DocumentoHce DOC = new DocumentoHce("12345678Z", "informe 1", "Informe", "Texto",
            LocalDateTime.of(2026, 11, 3, 10, 0));

    @Test
    @DisplayName("HCE: guarda los documentos de cada paciente; caída, rechaza la publicación sin guardarla")
    void hce() {
        HceSimulado hce = new HceSimulado();
        assertEquals("HCE", hce.nombre());
        hce.publicar(DOC);
        assertEquals(List.of(DOC), hce.documentos("12345678Z"));
        assertTrue(hce.documentos("87654321X").isEmpty());
        hce.setDisponible(false);
        assertFalse(hce.disponible());
        assertThrows(ReglaNegocioException.class, () -> hce.publicar(DOC));
        assertEquals(1, hce.documentos("12345678Z").size());
    }

    @Test
    @DisplayName("Admisión: devuelve su padrón (copia no modificable); caída, lanza ReglaNegocio")
    void admision() {
        PacienteAdmision rosa = new PacienteAdmision("11223344B", "Rosa", "Navarro", LocalDate.of(1992, 2, 20),
                "926123456", null);
        AdmisionSimulado admision = new AdmisionSimulado().registrar(rosa);
        assertEquals("Admisión", admision.nombre());
        assertEquals(List.of(rosa), admision.pacientes());
        assertThrows(UnsupportedOperationException.class, () -> admision.pacientes().clear());
        admision.setDisponible(false);
        assertThrows(ReglaNegocioException.class, admision::pacientes);
    }

    @Test
    @DisplayName("PACS: también es un sistema externo con nombre")
    void pacs() {
        assertEquals("RIS/PACS", new PacsSimulado().nombre());
    }
}
