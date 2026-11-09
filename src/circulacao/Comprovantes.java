package circulacao;

import java.time.LocalDate;
import java.util.List;

// Comprovantes que a fachada devolve: só dados simples, a interface nunca recebe
// entidades nem coleções do domínio.
public final class Comprovantes {
    private Comprovantes() {
    }

    public record ComprovanteReserva(String idReserva, String idLivro, String titulo, String idEstudante,
                                     LocalDate validaAte, String estadoLivro) { }

    public record ComprovanteEmprestimo(String idEmprestimo, String idLivro, String titulo, String idEstudante,
                                        LocalDate dataEmprestimo, LocalDate vencimento, String estadoLivro) { }

    public record ComprovanteRenovacao(String idEmprestimo, String idLivro, LocalDate novoVencimento,
                                       int renovacoesFeitas, int renovacoesRestantes, String estadoLivro) { }

    public record ComprovanteDevolucao(String idDevolucao, String idLivro, String idEstudante,
                                       LocalDate vencimento, LocalDate dataDevolucao, long diasAtraso,
                                       String estadoLivro, List<String> falhasNasReacoes) { }

    public record ComprovanteSituacaoLivro(String idLivro, String titulo, String estadoLivro) { }
}
