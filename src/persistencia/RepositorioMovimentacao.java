package persistencia;

import dominio.movimentacao.Devolucao;
import dominio.movimentacao.Emprestimo;
import dominio.movimentacao.Multa;
import dominio.movimentacao.Reserva;

import java.util.List;
import java.util.Optional;

// Padrão Abstract Factory - produto abstrato (reservas, empréstimos, devoluções e multas)
public interface RepositorioMovimentacao {

    void salvarReserva(Reserva reserva);

    Optional<Reserva> buscarReservaDoLivro(String idLivro);

    void removerReserva(String idReserva);

    void salvarEmprestimo(Emprestimo emprestimo);   // insere ou atualiza

    Optional<Emprestimo> buscarEmprestimoAtivo(String idLivro);

    void salvarDevolucao(Devolucao devolucao);

    Optional<Devolucao> buscarUltimaDevolucao(String idLivro, String idEstudante);

    void salvarMulta(Multa multa);                  // insere ou atualiza

    Optional<Multa> buscarMultaPorDevolucao(String idDevolucao);

    Optional<Multa> buscarMultaPorCobranca(String idCobranca);

    List<Multa> listarMultas();

    UnidadeDeTrabalho unidadeDeTrabalho();
}
