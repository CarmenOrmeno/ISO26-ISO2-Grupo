package es.uclm.esi.iso2.servidor.catalogo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.uclm.esi.iso2.servidor.comun.NoEncontradoException;
import es.uclm.esi.iso2.servidor.comun.ReglaNegocioException;
import es.uclm.esi.iso2.servidor.comun.RepositorioEnMemoria;

class ServicioCatalogoImplTest {

    private final ParametrosSistema parametros = new ParametrosSistema();
    private final ServicioCatalogo servicio = new ServicioCatalogoImpl(
            new RepositorioEnMemoria<>(TipoPrueba::codigo), parametros);

    private static TipoPrueba tipo(String codigo, Modalidad m, int min) {
        return new TipoPrueba(codigo, "Prueba " + codigo, m, "S1", min, "", false, false, "", false);
    }

    @Test
    @DisplayName("Alta, consulta, filtro por modalidad, modificación y baja")
    void ciclo() {
        servicio.alta(tipo("RX1", Modalidad.RX, 10));
        servicio.alta(tipo("TC1", Modalidad.TC, 30));
        assertEquals(2, servicio.listar().size());
        assertEquals(List.of("TC1"), servicio.listarPorModalidad(Modalidad.TC).stream().map(TipoPrueba::codigo).toList());
        servicio.modificar(tipo("RX1", Modalidad.RX, 15));
        assertEquals(15, servicio.obtener("RX1").duracionMinutos());
        servicio.baja("RX1");
        assertThrows(NoEncontradoException.class, () -> servicio.obtener("RX1"));
        assertSame(parametros, servicio.parametros());
    }

    @Test
    @DisplayName("Código repetido, modificar o borrar inexistente")
    void errores() {
        servicio.alta(tipo("RX1", Modalidad.RX, 10));
        assertThrows(ReglaNegocioException.class, () -> servicio.alta(tipo("RX1", Modalidad.US, 20)));
        assertThrows(NoEncontradoException.class, () -> servicio.modificar(tipo("XX", Modalidad.RX, 10)));
        assertThrows(NoEncontradoException.class, () -> servicio.baja("XX"));
    }
}
