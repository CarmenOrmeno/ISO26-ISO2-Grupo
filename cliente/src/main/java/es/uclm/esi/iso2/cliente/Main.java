package es.uclm.esi.iso2.cliente;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import es.uclm.esi.iso2.cliente.controlador.AplicacionCliente;
import es.uclm.esi.iso2.cliente.controlador.FabricaMenus;
import es.uclm.esi.iso2.cliente.vista.Consola;
import es.uclm.esi.iso2.servidor.Servidor;

/** Arranque del cliente con el servidor en proceso (comunicación simulada, D-007). */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        Consola consola = new Consola(
                new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)),
                new PrintStream(System.out, true, StandardCharsets.UTF_8));
        AplicacionCliente app = FabricaMenus.crear(consola, Servidor.conDatosDemo());
        app.ejecutar();
    }
}
