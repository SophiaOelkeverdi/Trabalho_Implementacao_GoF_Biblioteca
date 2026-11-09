package persistencia.memoria;

import dominio.movimentacao.Devolucao;
import dominio.movimentacao.Emprestimo;
import dominio.movimentacao.Multa;
import dominio.movimentacao.Reserva;
import persistencia.RepositorioMovimentacao;
import persistencia.UnidadeDeTrabalho;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

// Padrão Abstract Factory - produto concreto (família em memória)
public class RepositorioMovimentacaoMemoria implements RepositorioMovimentacao {
    private final UnidadeDeTrabalhoMemoria unidade;

    RepositorioMovimentacaoMemoria(UnidadeDeTrabalhoMemoria unidade) {
        this.unidade = unidade;
    }

    private BaseEmMemoria.Tabelas t() {
        unidade.exigirAtiva();
        return unidade.base().tabelas();
    }

    private void gravou() {
        unidade.base().contarGravacao();
    }

    // ---- reservas ----

    @Override
    public void salvarReserva(Reserva r) {
        t().reservas.put(r.getIdTransacao(), new BaseEmMemoria.ReservaReg(r.getIdTransacao(), r.getData(),
                r.getIdLivro(), r.getIdEstudante(), r.getValidaAte()));
        gravou();
    }

    @Override
    public Optional<Reserva> buscarReservaDoLivro(String idLivro) {
        return t().reservas.values().stream()
                .filter(r -> r.idLivro().equals(idLivro))
                .findFirst()
                .map(r -> new Reserva(r.id(), r.data(), r.idLivro(), r.idEstudante(), r.validaAte()));
    }

    @Override
    public void removerReserva(String idReserva) {
        t().reservas.remove(idReserva);
        gravou();
    }

    // ---- empréstimos ----

    @Override
    public void salvarEmprestimo(Emprestimo e) {
        t().emprestimos.put(e.getIdTransacao(), new BaseEmMemoria.EmprestimoReg(e.getIdTransacao(), e.getData(),
                e.getIdLivro(), e.getIdEstudante(), e.getDataVencimento(), e.getNumRenovacoes(), e.isAtivo()));
        gravou();
    }

    @Override
    public Optional<Emprestimo> buscarEmprestimoAtivo(String idLivro) {
        return t().emprestimos.values().stream()
                .filter(e -> e.idLivro().equals(idLivro) && e.ativo())
                .findFirst()
                .map(e -> new Emprestimo(e.id(), e.data(), e.idLivro(), e.idEstudante(), e.vencimento(),
                        e.numRenovacoes(), e.ativo()));
    }

    // ---- devoluções ----

    @Override
    public void salvarDevolucao(Devolucao d) {
        t().devolucoes.put(d.getIdTransacao(), new BaseEmMemoria.DevolucaoReg(d.getIdTransacao(), d.getData(),
                d.getIdLivro(), d.getIdEstudante(), d.getIdEmprestimo(), d.getVencimento()));
        gravou();
    }

    @Override
    public Optional<Devolucao> buscarUltimaDevolucao(String idLivro, String idEstudante) {
        return t().devolucoes.values().stream()
                .filter(d -> d.idLivro().equals(idLivro) && d.idEstudante().equals(idEstudante))
                .max(Comparator.comparing(BaseEmMemoria.DevolucaoReg::data))
                .map(d -> new Devolucao(d.id(), d.data(), d.idLivro(), d.idEstudante(), d.idEmprestimo(),
                        d.vencimento()));
    }

    // ---- multas ----

    @Override
    public void salvarMulta(Multa m) {
        t().multas.put(m.getId(), new BaseEmMemoria.MultaReg(m.getId(), m.getIdDevolucao(), m.getIdEstudante(),
                m.getDiasAtraso(), m.getValor(), m.getPoliticaAplicada(), m.getEmitidaEm(), m.getIdCobranca(),
                m.getSituacao().name()));
        gravou();
    }

    @Override
    public Optional<Multa> buscarMultaPorDevolucao(String idDevolucao) {
        return t().multas.values().stream().filter(m -> m.idDevolucao().equals(idDevolucao)).findFirst()
                .map(RepositorioMovimentacaoMemoria::paraMulta);
    }

    @Override
    public Optional<Multa> buscarMultaPorCobranca(String idCobranca) {
        return t().multas.values().stream().filter(m -> idCobranca.equals(m.idCobranca())).findFirst()
                .map(RepositorioMovimentacaoMemoria::paraMulta);
    }

    @Override
    public List<Multa> listarMultas() {
        return t().multas.values().stream().map(RepositorioMovimentacaoMemoria::paraMulta).toList();
    }

    private static Multa paraMulta(BaseEmMemoria.MultaReg m) {
        return new Multa(m.id(), m.idDevolucao(), m.idEstudante(), m.diasAtraso(), m.valor(), m.politica(),
                m.emitidaEm(), m.idCobranca(), Multa.Situacao.valueOf(m.situacao()));
    }

    @Override
    public UnidadeDeTrabalho unidadeDeTrabalho() {
        return unidade;
    }
}
