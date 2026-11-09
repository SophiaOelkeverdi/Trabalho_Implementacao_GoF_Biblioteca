package dominio.movimentacao;

import java.time.LocalDate;

// Transação de circulação: envolve um livro e é solicitada por um estudante
public abstract class Transacao {
    private final String idTransacao;
    private final LocalDate data;
    private final String idLivro;
    private final String idEstudante;

    protected Transacao(String idTransacao, LocalDate data, String idLivro, String idEstudante) {
        this.idTransacao = idTransacao;
        this.data = data;
        this.idLivro = idLivro;
        this.idEstudante = idEstudante;
    }

    public String getIdTransacao() { return idTransacao; }
    public LocalDate getData() { return data; }
    public String getIdLivro() { return idLivro; }
    public String getIdEstudante() { return idEstudante; }
}
