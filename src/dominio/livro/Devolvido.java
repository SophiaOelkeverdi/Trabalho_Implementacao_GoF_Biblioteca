package dominio.livro;

import dominio.Ids;
import dominio.movimentacao.Devolucao;
import dominio.movimentacao.Emprestimo;

import java.time.LocalDate;

// Padrão State - estado concreto DEVOLVIDO.
// Fica aqui até a inspeção decidir. Não aceita reserva (cenário do R01).
public final class Devolvido extends EstadoLivro {
    public static final String NOME = "DEVOLVIDO";
    public static final Devolvido INSTANCIA = new Devolvido();

    private Devolvido() {
    }

    @Override
    public String nome() { return NOME; }

    // inspecionarLivro [bom estado] -> LIVRE ; [danificado] -> REPARANDO
    @Override
    public void inspecionar(Livro livro, boolean bomEstado) {
        livro.alterarEstado(bomEstado ? Livre.INSTANCIA : Reparando.INSTANCIA);
    }

    // parte comum das transições EMPRESTADO/RENOVADO -> DEVOLVIDO
    static Devolucao receber(Livro livro, Emprestimo emprestimo, LocalDate hoje, String estadoOrigem) {
        if (emprestimo == null || !emprestimo.isAtivo() || !emprestimo.getIdLivro().equals(livro.getId())) {
            throw new TransicaoRecusadaException("registrarDevolucao", estadoOrigem, "emprestimo nao registrado");
        }
        Devolucao devolucao = new Devolucao(Ids.novo("DEV"), hoje, livro.getId(), emprestimo.getIdEstudante(),
                emprestimo.getIdTransacao(), emprestimo.getDataVencimento());
        emprestimo.encerrar();
        livro.alterarEstado(INSTANCIA);
        return devolucao;
    }
}
