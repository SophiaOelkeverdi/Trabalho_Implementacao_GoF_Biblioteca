package dominio.livro;

import dominio.RegrasCirculacao;
import dominio.movimentacao.Devolucao;
import dominio.movimentacao.Emprestimo;

import java.time.LocalDate;

// Padrão State - estado concreto EMPRESTADO
public final class Emprestado extends EstadoLivro {
    public static final String NOME = "EMPRESTADO";
    public static final Emprestado INSTANCIA = new Emprestado();

    private Emprestado() {
    }

    @Override
    public String nome() { return NOME; }

    // renovarEmprestimo [numRenovacoes < 2] / estende 30 dias -> RENOVADO
    // a guarda vem ANTES da ação, então se for recusada o vencimento não muda
    @Override
    public void renovar(Livro livro, Emprestimo emprestimo) {
        if (!emprestimo.podeRenovar()) {
            throw new TransicaoRecusadaException("renovarEmprestimo", NOME, "limite de renovacoes atingido");
        }
        emprestimo.estenderVencimento(RegrasCirculacao.DIAS_POR_RENOVACAO);
        livro.alterarEstado(Renovado.INSTANCIA);
    }

    // registrarDevolucao [emprestimo registrado] / registrarDataDevolucao -> DEVOLVIDO
    @Override
    public Devolucao registrarDevolucao(Livro livro, Emprestimo emprestimo, LocalDate hoje) {
        return Devolvido.receber(livro, emprestimo, hoje, NOME);
    }
}
