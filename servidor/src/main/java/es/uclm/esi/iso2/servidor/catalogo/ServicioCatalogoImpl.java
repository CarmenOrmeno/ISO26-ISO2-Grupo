package es.uclm.esi.iso2.servidor.catalogo;

import java.util.List;

import es.uclm.esi.iso2.servidor.comun.ReglaNegocioException;
import es.uclm.esi.iso2.servidor.comun.Repositorio;

public class ServicioCatalogoImpl implements ServicioCatalogo {

    private final Repositorio<TipoPrueba, String> tipos;
    private final ParametrosSistema parametros;

    public ServicioCatalogoImpl(Repositorio<TipoPrueba, String> tipos, ParametrosSistema parametros) {
        this.tipos = tipos;
        this.parametros = parametros;
    }

    @Override
    public TipoPrueba alta(TipoPrueba tipo) {
        if (tipos.existe(tipo.codigo())) {
            throw new ReglaNegocioException("Ya existe el tipo de prueba " + tipo.codigo());
        }
        return tipos.guardar(tipo);
    }

    @Override
    public TipoPrueba modificar(TipoPrueba tipo) {
        tipos.obtener(tipo.codigo());
        return tipos.guardar(tipo);
    }

    @Override
    public void baja(String codigo) {
        tipos.eliminar(codigo);
    }

    @Override
    public TipoPrueba obtener(String codigo) {
        return tipos.obtener(codigo);
    }

    @Override
    public List<TipoPrueba> listar() {
        return tipos.todos();
    }

    @Override
    public List<TipoPrueba> listarPorModalidad(Modalidad modalidad) {
        return tipos.filtrar(t -> t.modalidad() == modalidad);
    }

    @Override
    public ParametrosSistema parametros() {
        return parametros;
    }
}
