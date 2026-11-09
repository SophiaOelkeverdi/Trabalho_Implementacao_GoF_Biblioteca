package dominio.livro;

import dominio.Ids;
import dominio.RegrasCirculacao;
import dominio.movimentacao.Emprestimo;
import dominio.movimentacao.Reserva;

import java.time.LocalDate;

// Padrão State - estado concreto RESERVADO
public final class Reservado extends EstadoLivro {
    public static final String NOME = "RESERVADO";
    public static final Reservado INSTANCIA = new Reservado();

    private Reservado() {
    }

    @Override
    public String nome() { return NOME; }

    // emprestarLivro [mesmo estudante da reserva] / calcularDataDevolucao -> EMPRESTADO
    @Override
    public Emprestimo emprestar(Livro livro, Reserva reserva, String idEstudante, LocalDate hoje) {
        if (reserva == null || !reserva.getIdLivro().equals(livro.getId()) || !reserva.pertenceA(idEstudante)) {
            throw new TransicaoRecusadaException("emprestarLivro", NOME,
                    "o livro esta reservado para outro estudante");
        }
        Emprestimo emprestimo = new Emprestimo(Ids.novo("EMP"), hoje, livro.getId(), idEstudante,
                hoje.plusDays(RegrasCirculacao.PRAZO_EMPRESTIMO_DIAS), 0, true);
        livro.alterarEstado(Emprestado.INSTANCIA);
        return emprestimo;
    }

    // removerReserva -> LIVRE
    @Override
    public void removerReserva(Livro livro) {
        livro.alterarEstado(Livre.INSTANCIA);
    }
}
