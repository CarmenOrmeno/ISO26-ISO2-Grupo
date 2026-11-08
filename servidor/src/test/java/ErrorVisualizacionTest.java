import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ErrorVisualizacionTest {
    @Test
    void muestraUnErrorIntencionado() {
        assertEquals(2, 1, "Fallo intencionado para comprobar la visualización del error");
    }
}
