package es.uclm.esi.iso2.servidor;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.Test;

class SolicitudPruebaTest {
    @Test
    void unaSolicitudNuevaNoTieneInforme() {
        SolicitudPrueba solicitud = new SolicitudPrueba(1);

        assertNotNull(solicitud);
        assertNull(solicitud.getInforme());
    }

    @Test
    void solicitudesConLaMismaPrioridadSonObjetosDistintos() {
        SolicitudPrueba primera = new SolicitudPrueba(1);
        SolicitudPrueba segunda = new SolicitudPrueba(1);

        assertSame(primera, primera);
        assertNotSame(primera, segunda);
    }

    @Test
    void registrarRealizacionConservaLaPrioridad() {
        int prioridadInicial = 1;
        SolicitudPrueba solicitud = new SolicitudPrueba(prioridadInicial);

        solicitud.registrarRealizacion();

        assertAll(
                () -> assertEquals(SolicitudPrueba.Estado.REALIZADA, solicitud.getEstado()),
                () -> assertEquals(prioridadInicial, solicitud.getPrioridad()));
    }

    @Test
    void muestraUnMensaje() {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        PrintStream salidaOriginal = System.out;

        try {
            System.setOut(new PrintStream(salida));
            SolicitudPrueba.main(new String[0]);
        } finally {
            System.setOut(salidaOriginal);
        }

        assertEquals("Solicitud de prueba ejecutada" + System.lineSeparator(), salida.toString());
    }
}
