package es.uclm.esi.iso2.cliente.controlador;

import es.uclm.esi.iso2.cliente.vista.Consola;
import es.uclm.esi.iso2.servidor.Servidor;
import es.uclm.esi.iso2.servidor.seguridad.Rol;

/** Construye la aplicación cliente con un menú por rol a partir de las API del servidor. */
public final class FabricaMenus {

    private FabricaMenus() {
    }

    public static AplicacionCliente crear(Consola consola, Servidor servidor) {
        return new AplicacionCliente(consola, servidor.sesion())
                .conMenu(Rol.PACIENTE, new Menu("Menú del paciente", consola, servidor.sesion()))
                .conMenu(Rol.MEDICO, new Menu("Menú del médico", consola, servidor.sesion()))
                .conMenu(Rol.ADMINISTRATIVO, new Menu("Menú de admisión", consola, servidor.sesion()))
                .conMenu(Rol.ADMINISTRADOR, new Menu("Menú del administrador", consola, servidor.sesion()));
    }
}
