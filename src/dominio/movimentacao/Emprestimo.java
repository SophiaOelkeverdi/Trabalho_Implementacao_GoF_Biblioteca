package dominio.movimentacao;

import dominio.RegrasCirculacao;

import java.time.LocalDate;

public class Emprestimo extends Transacao {
    private LocalDate dataVencimento;
    private int numRenovacoes;
    private boolean ativo;

    public Emprestimo(String id, LocalDate data, String idLivro, String idEstudante,
                      LocalDate dataVencimento, int numRenovacoes, boolean ativo) {
        super(id, data, idLivro, idEstudante);
        this.dataVencimento = dataVencimento;
        this.numRenovacoes = numRenovacoes;
        this.ativo = ativo;
    }

    // guarda da renovação (usada pelos estados EMPRESTADO e RENOVADO)
    public boolean podeRenovar() {
        return numRenovacoes < RegrasCirculacao.LIMITE_RENOVACOES;
    }

    // ação da renovação: atualizarVencimento + incrementarRenovacoes
    public void estenderVencimento(int dias) {
        this.dataVencimento = dataVencimento.plusDays(dias);
        this.numRenovacoes++;
    }

    public void encerrar() {
        this.ativo = false;
    }

    public LocalDate getDataVencimento() { return dataVencimento; }
    public int getNumRenovacoes() { return numRenovacoes; }
    public boolean isAtivo() { return ativo; }

    @Override
    public String toString() {
        return "Emprestimo " + getIdTransacao() + " livro=" + getIdLivro() + " estudante=" + getIdEstudante()
                + " vence " + dataVencimento + " renovacoes=" + numRenovacoes;
    }
}
