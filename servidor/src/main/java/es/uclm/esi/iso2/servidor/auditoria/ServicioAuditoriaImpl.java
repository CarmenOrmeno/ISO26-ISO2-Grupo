package es.uclm.esi.iso2.servidor.auditoria;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

import es.uclm.esi.iso2.servidor.comun.Repositorio;
import es.uclm.esi.iso2.servidor.comun.Secuencia;
import es.uclm.esi.iso2.servidor.comun.Validaciones;
import es.uclm.esi.iso2.servidor.seguridad.Permiso;
import es.uclm.esi.iso2.servidor.seguridad.Usuario;

public class ServicioAuditoriaImpl implements ServicioAuditoria {

    private final Repositorio<RegistroAuditoria, Long> registros;
    private final Clock reloj;
    private final Secuencia secuencia;

    public ServicioAuditoriaImpl(Repositorio<RegistroAuditoria, Long> registros, Clock reloj) {
        this.registros = registros;
        this.reloj = reloj;
        this.secuencia = Secuencia.continuando(registros, RegistroAuditoria::id);
    }

    @Override
    public void registrar(Usuario usuario, Permiso permiso, String recurso, boolean permitido) {
        RegistroAuditoria registro = new RegistroAuditoria(secuencia.siguiente(), LocalDateTime.now(reloj),
                usuario.login(), usuario.rol(), permiso, recurso, permitido);
        secuencia.confirmar();
        registros.guardar(registro);
    }

    @Override
    public List<RegistroAuditoria> registros(LocalDate desde, LocalDate hasta) {
        return buscar(desde, hasta, r -> true);
    }

    @Override
    public List<RegistroAuditoria> deUsuario(String login, LocalDate desde, LocalDate hasta) {
        String buscado = Validaciones.texto(login, "login");
        return buscar(desde, hasta, r -> r.login().equals(buscado));
    }

    @Override
    public List<RegistroAuditoria> denegados(LocalDate desde, LocalDate hasta) {
        return buscar(desde, hasta, r -> !r.permitido());
    }

    @Override
    public List<RegistroAuditoria> sobreRecurso(String texto, LocalDate desde, LocalDate hasta) {
        String buscado = Validaciones.texto(texto, "recurso").toLowerCase(Locale.ROOT);
        return buscar(desde, hasta, r -> r.recurso().toLowerCase(Locale.ROOT).contains(buscado));
    }

    private List<RegistroAuditoria> buscar(LocalDate desde, LocalDate hasta, Predicate<RegistroAuditoria> condicion) {
        Validaciones.obligatorio(desde, "desde");
        Validaciones.obligatorio(hasta, "hasta");
        if (hasta.isBefore(desde)) {
            throw new IllegalArgumentException("La fecha final no puede ser anterior a la inicial");
        }
        return registros.filtrar(r -> {
            LocalDate dia = r.fecha().toLocalDate();
            return !dia.isBefore(desde) && !dia.isAfter(hasta) && condicion.test(r);
        }).stream().sorted(Comparator.comparing(RegistroAuditoria::id).reversed()).toList();
    }
}
