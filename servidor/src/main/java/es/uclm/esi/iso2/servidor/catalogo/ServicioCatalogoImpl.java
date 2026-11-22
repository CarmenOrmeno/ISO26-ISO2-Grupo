package es.uclm.esi.iso2.servidor.catalogo;

import java.time.LocalTime;
import java.util.List;

import es.uclm.esi.iso2.servidor.comun.ReglaNegocioException;
import es.uclm.esi.iso2.servidor.comun.Repositorio;

public class ServicioCatalogoImpl implements ServicioCatalogo {

    /** Id del único registro de parámetros del sistema en su almacén. */
    public static final String ID_PARAMETROS = "sistema";

    private final Repositorio<TipoPrueba, String> tipos;
    private final Repositorio<ParametrosSistema, String> almacenParametros;
    private final ParametrosSistema parametros;

    /** Usa los parámetros guardados o, la primera vez, guarda los de por defecto. */
    public ServicioCatalogoImpl(Repositorio<TipoPrueba, String> tipos,
            Repositorio<ParametrosSistema, String> almacenParametros) {
        this.tipos = tipos;
        this.almacenParametros = almacenParametros;
        this.parametros = almacenParametros.buscar(ID_PARAMETROS)
                .orElseGet(() -> almacenParametros.guardar(new ParametrosSistema()));
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

    @Override
    public void cambiarHorario(LocalTime apertura, LocalTime cierre) {
        parametros.cambiarHorario(apertura, cierre);
        almacenParametros.guardar(parametros);
    }

    @Override
    public void cambiarPlazo(Prioridad prioridad, int dias) {
        parametros.cambiarPlazo(prioridad, dias);
        almacenParametros.guardar(parametros);
    }

    @Override
    public void cambiarHorasRecordatorio(int horas) {
        parametros.cambiarHorasRecordatorio(horas);
        almacenParametros.guardar(parametros);
    }
}
