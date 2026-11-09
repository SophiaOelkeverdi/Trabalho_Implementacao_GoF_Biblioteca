package acesso;

import dominio.RegraDeNegocioException;
import dominio.movimentacao.Devolucao;
import dominio.movimentacao.Emprestimo;
import dominio.movimentacao.Multa;
import dominio.movimentacao.Reserva;
import persistencia.RepositorioMovimentacao;

import java.util.List;
import java.util.Optional;

// Ponto interno de acesso "Movimentacao" (do Trabalho 02): reservas, empréstimos e devoluções
public class Movimentacao {
    private final RepositorioMovimentacao repositorio;

    public Movimentacao(RepositorioMovimentacao repositorio) {
        this.repositorio = repositorio;
    }

    // ---- reservas ----

    public void adicionarReserva(Reserva reserva) {
        repositorio.salvarReserva(reserva);
    }

    public Optional<Reserva> consultarReservaDoLivro(String idLivro) {
        return repositorio.buscarReservaDoLivro(idLivro);
    }

    // a reserva do livro tem que ser deste estudante
    public Reserva obterReserva(String idLivro, String idEstudante) {
        return repositorio.buscarReservaDoLivro(idLivro)
                .filter(r -> r.pertenceA(idEstudante))
                .orElseThrow(() -> new RegraDeNegocioException(
                        "Reserva inexistente para o livro " + idLivro + " e o estudante " + idEstudante));
    }

    public void removerReserva(Reserva reserva) {
        repositorio.removerReserva(reserva.getIdTransacao());
    }

    // ---- empréstimos ----

    public void inserirEmprestimo(Emprestimo emprestimo) {
        repositorio.salvarEmprestimo(emprestimo);
    }

    public void atualizarEmprestimo(Emprestimo emprestimo) {
        repositorio.salvarEmprestimo(emprestimo);
    }

    public Optional<Emprestimo> consultarEmprestimoAtivo(String idLivro) {
        return repositorio.buscarEmprestimoAtivo(idLivro);
    }

    public Emprestimo obterEmprestimo(String idLivro, String idEstudante) {
        return repositorio.buscarEmprestimoAtivo(idLivro)
                .filter(e -> e.getIdEstudante().equals(idEstudante))
                .orElseThrow(() -> new RegraDeNegocioException(
                        "Emprestimo nao encontrado para o livro " + idLivro + " e o estudante " + idEstudante));
    }

    // ---- devoluções e multas ----

    public void inserirDevolucao(Devolucao devolucao) {
        repositorio.salvarDevolucao(devolucao);
    }

    public Devolucao obterUltimaDevolucao(String idLivro, String idEstudante) {
        return repositorio.buscarUltimaDevolucao(idLivro, idEstudante)
                .orElseThrow(() -> new RegraDeNegocioException("Devolucao nao encontrada"));
    }

    public void salvarMulta(Multa multa) {
        repositorio.salvarMulta(multa);
    }

    public Optional<Multa> consultarMultaDaDevolucao(String idDevolucao) {
        return repositorio.buscarMultaPorDevolucao(idDevolucao);
    }

    public Optional<Multa> consultarMultaPorCobranca(String idCobranca) {
        return repositorio.buscarMultaPorCobranca(idCobranca);
    }

    public List<Multa> listarMultas() {
        return repositorio.listarMultas();
    }
}
