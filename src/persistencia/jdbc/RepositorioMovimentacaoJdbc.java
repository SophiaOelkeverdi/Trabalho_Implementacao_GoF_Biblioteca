package persistencia.jdbc;

import dominio.movimentacao.Devolucao;
import dominio.movimentacao.Emprestimo;
import dominio.movimentacao.Multa;
import dominio.movimentacao.Reserva;
import persistencia.RepositorioMovimentacao;
import persistencia.UnidadeDeTrabalho;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

// Padrão Abstract Factory - produto concreto (família MySQL)
public class RepositorioMovimentacaoJdbc implements RepositorioMovimentacao {
    private final UnidadeDeTrabalhoJdbc unidade;

    RepositorioMovimentacaoJdbc(UnidadeDeTrabalhoJdbc unidade) {
        this.unidade = unidade;
    }

    private Connection c() {
        return unidade.conexao();
    }

    // ---- reservas ----

    @Override
    public void salvarReserva(Reserva r) {
        Sql.executar(c(), "INSERT INTO reserva (id, data, id_livro, id_estudante, valida_ate) VALUES (?, ?, ?, ?, ?)",
                r.getIdTransacao(), r.getData(), r.getIdLivro(), r.getIdEstudante(), r.getValidaAte());
    }

    @Override
    public Optional<Reserva> buscarReservaDoLivro(String idLivro) {
        return Sql.um(c(), "SELECT * FROM reserva WHERE id_livro = ?", rs -> new Reserva(rs.getString("id"),
                Sql.data(rs, "data"), rs.getString("id_livro"), rs.getString("id_estudante"),
                Sql.data(rs, "valida_ate")), idLivro);
    }

    @Override
    public void removerReserva(String idReserva) {
        Sql.executar(c(), "DELETE FROM reserva WHERE id = ?", idReserva);
    }

    // ---- empréstimos ----

    @Override
    public void salvarEmprestimo(Emprestimo e) {
        Sql.salvar(c(),
                "UPDATE emprestimo SET vencimento = ?, num_renovacoes = ?, ativo = ? WHERE id = ?",
                new Object[]{e.getDataVencimento(), e.getNumRenovacoes(), e.isAtivo(), e.getIdTransacao()},
                "INSERT INTO emprestimo (id, data, id_livro, id_estudante, vencimento, num_renovacoes, ativo) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                new Object[]{e.getIdTransacao(), e.getData(), e.getIdLivro(), e.getIdEstudante(),
                        e.getDataVencimento(), e.getNumRenovacoes(), e.isAtivo()});
    }

    @Override
    public Optional<Emprestimo> buscarEmprestimoAtivo(String idLivro) {
        return Sql.um(c(), "SELECT * FROM emprestimo WHERE id_livro = ? AND ativo = TRUE",
                rs -> new Emprestimo(rs.getString("id"), Sql.data(rs, "data"), rs.getString("id_livro"),
                        rs.getString("id_estudante"), Sql.data(rs, "vencimento"), rs.getInt("num_renovacoes"),
                        rs.getBoolean("ativo")), idLivro);
    }

    // ---- devoluções ----

    @Override
    public void salvarDevolucao(Devolucao d) {
        Sql.executar(c(), "INSERT INTO devolucao (id, data, id_livro, id_estudante, id_emprestimo, vencimento) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                d.getIdTransacao(), d.getData(), d.getIdLivro(), d.getIdEstudante(), d.getIdEmprestimo(),
                d.getVencimento());
    }

    @Override
    public Optional<Devolucao> buscarUltimaDevolucao(String idLivro, String idEstudante) {
        return Sql.um(c(), "SELECT * FROM devolucao WHERE id_livro = ? AND id_estudante = ? ORDER BY data DESC",
                rs -> new Devolucao(rs.getString("id"), Sql.data(rs, "data"), rs.getString("id_livro"),
                        rs.getString("id_estudante"), rs.getString("id_emprestimo"), Sql.data(rs, "vencimento")),
                idLivro, idEstudante);
    }

    // ---- multas ----

    @Override
    public void salvarMulta(Multa m) {
        Sql.salvar(c(),
                "UPDATE multa SET id_cobranca = ?, situacao = ? WHERE id = ?",
                new Object[]{m.getIdCobranca(), m.getSituacao().name(), m.getId()},
                "INSERT INTO multa (id, id_devolucao, id_estudante, dias_atraso, valor, politica, emitida_em, "
                        + "id_cobranca, situacao) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                new Object[]{m.getId(), m.getIdDevolucao(), m.getIdEstudante(), m.getDiasAtraso(), m.getValor(),
                        m.getPoliticaAplicada(), m.getEmitidaEm(), m.getIdCobranca(), m.getSituacao().name()});
    }

    @Override
    public Optional<Multa> buscarMultaPorDevolucao(String idDevolucao) {
        return Sql.um(c(), "SELECT * FROM multa WHERE id_devolucao = ?", RepositorioMovimentacaoJdbc::paraMulta,
                idDevolucao);
    }

    @Override
    public Optional<Multa> buscarMultaPorCobranca(String idCobranca) {
        return Sql.um(c(), "SELECT * FROM multa WHERE id_cobranca = ?", RepositorioMovimentacaoJdbc::paraMulta,
                idCobranca);
    }

    @Override
    public List<Multa> listarMultas() {
        return Sql.listar(c(), "SELECT * FROM multa ORDER BY emitida_em", RepositorioMovimentacaoJdbc::paraMulta);
    }

    private static Multa paraMulta(ResultSet rs) throws SQLException {
        return new Multa(rs.getString("id"), rs.getString("id_devolucao"), rs.getString("id_estudante"),
                rs.getLong("dias_atraso"), rs.getBigDecimal("valor"), rs.getString("politica"),
                Sql.data(rs, "emitida_em"), rs.getString("id_cobranca"),
                Multa.Situacao.valueOf(rs.getString("situacao")));
    }

    @Override
    public UnidadeDeTrabalho unidadeDeTrabalho() {
        return unidade;
    }
}
