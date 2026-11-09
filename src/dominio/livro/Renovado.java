package dominio.livro;

import dominio.RegrasCirculacao;
import dominio.movimentacao.Devolucao;
import dominio.movimentacao.Emprestimo;

import java.time.LocalDate;

// Padrão State - estado concreto RENOVADO
public final class Renovado extends EstadoLivro {
    public static final String NOME = "RENOVADO";
    public static final Renovado INSTANCIA = new Renovado();

    private Renovado() {
    }

    @Override
    public String nome() { return NOME; }

    // renovarEmprestimo [numRenovacoes < 2] / estende 30 dias -> continua RENOVADO
    // na 3a tentativa a guarda é falsa e o vencimento fica igual
    @Override
    public void renovar(Livro livro, Emprestimo emprestimo) {
        if (!emprestimo.podeRenovar()) {
            throw new TransicaoRecusadaException("renovarEmprestimo", NOME, "limite de renovacoes atingido");
        }
        emprestimo.estenderVencimento(RegrasCirculacao.DIAS_POR_RENOVACAO);
    }

    // registrarDevolucao [emprestimo registrado] / registrarDataDevolucao -> DEVOLVIDO
    @Override
    public Devolucao registrarDevolucao(Livro livro, Emprestimo emprestimo, LocalDate hoje) {
        return Devolvido.receber(livro, emprestimo, hoje, NOME);
    }
}
