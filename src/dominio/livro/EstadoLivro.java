package dominio.livro;

import dominio.movimentacao.Devolucao;
import dominio.movimentacao.Emprestimo;
import dominio.movimentacao.Reserva;
import dominio.pessoa.Estudante;

import java.time.LocalDate;

// Padrão State - papel State.
// Tem todos os eventos da máquina de estados. Por padrão todo evento é RECUSADO (lança exceção
// sem mexer no livro). Cada estado concreto sobrescreve só os eventos que aceita, e a guarda e a
// ação da transição ficam dentro desse método.
// Os estados não guardam dados, então cada um tem uma única instância (INSTANCIA).
public abstract class EstadoLivro {

    // nome que vai pro banco de dados (coluna "estado")
    public abstract String nome();

    public Reserva reservar(Livro livro, Estudante estudante, LocalDate hoje) {
        throw recusar("reservarLivro");
    }

    public void removerReserva(Livro livro) {
        throw recusar("removerReserva");
    }

    public Emprestimo emprestar(Livro livro, Reserva reserva, String idEstudante, LocalDate hoje) {
        throw recusar("emprestarLivro");
    }

    public void renovar(Livro livro, Emprestimo emprestimo) {
        throw recusar("renovarEmprestimo");
    }

    public Devolucao registrarDevolucao(Livro livro, Emprestimo emprestimo, LocalDate hoje) {
        throw recusar("registrarDevolucao");
    }

    public void inspecionar(Livro livro, boolean bomEstado) {
        throw recusar("inspecionarLivro");
    }

    public void registrarDano(Livro livro) {
        throw recusar("registrarDano");
    }

    public void concluirReparo(Livro livro) {
        throw recusar("avaliarReparo [consertado]");
    }

    public void registrarLaudoIrrecuperavel(Livro livro) {
        throw recusar("laudoIrrecuperavel");
    }

    public void excluir(Livro livro) {
        throw recusar("excluirLivro");
    }

    protected TransicaoRecusadaException recusar(String evento) {
        return new TransicaoRecusadaException(evento, nome());
    }

    // Converte o texto salvo no banco de volta para o objeto de estado.
    // É o único switch sobre estado no sistema todo.
    public static EstadoLivro porNome(String nome) {
        return switch (nome) {
            case Livre.NOME -> Livre.INSTANCIA;
            case Reservado.NOME -> Reservado.INSTANCIA;
            case Emprestado.NOME -> Emprestado.INSTANCIA;
            case Renovado.NOME -> Renovado.INSTANCIA;
            case Devolvido.NOME -> Devolvido.INSTANCIA;
            case Reparando.NOME -> Reparando.INSTANCIA;
            case Incinerado.NOME -> Incinerado.INSTANCIA;
            default -> throw new IllegalArgumentException("Estado de livro desconhecido: " + nome);
        };
    }

    @Override
    public String toString() {
        return nome();
    }
}
