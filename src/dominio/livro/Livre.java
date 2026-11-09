package dominio.livro;

import dominio.Ids;
import dominio.RegrasCirculacao;
import dominio.movimentacao.Reserva;
import dominio.pessoa.Estudante;

import java.time.LocalDate;

// Padrão State - estado concreto LIVRE
public final class Livre extends EstadoLivro {
    public static final String NOME = "LIVRE";
    public static final Livre INSTANCIA = new Livre();

    private Livre() {
    }

    @Override
    public String nome() { return NOME; }

    // reservarLivro [estudante cadastrado] / registrarVencimentoReserva -> RESERVADO
    @Override
    public Reserva reservar(Livro livro, Estudante estudante, LocalDate hoje) {
        if (estudante == null) {
            throw new TransicaoRecusadaException("reservarLivro", NOME, "estudante nao cadastrado");
        }
        Reserva reserva = new Reserva(Ids.novo("RES"), hoje, livro.getId(), estudante.getId(),
                hoje.plusDays(RegrasCirculacao.VALIDADE_RESERVA_DIAS));
        livro.alterarEstado(Reservado.INSTANCIA);
        return reserva;
    }

    // registrarDano [dano identificado] -> REPARANDO
    @Override
    public void registrarDano(Livro livro) {
        livro.alterarEstado(Reparando.INSTANCIA);
    }

    // excluirLivro -> estado final (o livro sai do Acervo). Só aceita quando está LIVRE.
    @Override
    public void excluir(Livro livro) {
    }
}
