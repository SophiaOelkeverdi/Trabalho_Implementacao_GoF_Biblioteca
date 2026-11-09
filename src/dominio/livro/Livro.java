package dominio.livro;

import dominio.movimentacao.Devolucao;
import dominio.movimentacao.Emprestimo;
import dominio.movimentacao.Reserva;
import dominio.pessoa.Estudante;

import java.time.LocalDate;

// Padrão State - papel Context.
// O Livro não tem nenhum if sobre a própria situação: cada evento é repassado pro objeto de
// estado atual, que decide se aceita e troca o estado.
public class Livro {
    private final String id;
    private String titulo;
    private String autor;
    private EstadoLivro estado;

    // inserirLivro: todo livro novo começa LIVRE
    public Livro(String id, String titulo, String autor) {
        this(id, titulo, autor, Livre.INSTANCIA);
    }

    private Livro(String id, String titulo, String autor, EstadoLivro estado) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.estado = estado;
    }

    // usado pelos repositórios: o estado veio do banco como texto
    public static Livro reconstituir(String id, String titulo, String autor, String nomeEstado) {
        return new Livro(id, titulo, autor, EstadoLivro.porNome(nomeEstado));
    }

    // só as classes de estado (mesmo pacote) podem trocar o estado
    void alterarEstado(EstadoLivro novo) {
        this.estado = novo;
    }

    // ---- eventos: todos delegam pro estado ----

    public Reserva reservar(Estudante estudante, LocalDate hoje) {
        return estado.reservar(this, estudante, hoje);
    }

    public void removerReserva() {
        estado.removerReserva(this);
    }

    public Emprestimo emprestar(Reserva reserva, String idEstudante, LocalDate hoje) {
        return estado.emprestar(this, reserva, idEstudante, hoje);
    }

    public void renovar(Emprestimo emprestimo) {
        estado.renovar(this, emprestimo);
    }

    public Devolucao registrarDevolucao(Emprestimo emprestimo, LocalDate hoje) {
        return estado.registrarDevolucao(this, emprestimo, hoje);
    }

    public void inspecionar(boolean bomEstado) {
        estado.inspecionar(this, bomEstado);
    }

    public void registrarDano() {
        estado.registrarDano(this);
    }

    public void concluirReparo() {
        estado.concluirReparo(this);
    }

    public void registrarLaudoIrrecuperavel() {
        estado.registrarLaudoIrrecuperavel(this);
    }

    public void excluir() {
        estado.excluir(this);
    }

    public void atualizarDados(String titulo, String autor) {
        this.titulo = titulo;
        this.autor = autor;
    }

    public String getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getAutor() { return autor; }
    public String getEstado() { return estado.nome(); }

    @Override
    public String toString() {
        return "Livro " + id + " \"" + titulo + "\" (" + autor + ") [" + estado.nome() + "]";
    }
}
