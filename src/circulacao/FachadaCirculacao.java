package circulacao;

import acesso.ExecutorTransacional;
import circulacao.Comprovantes.ComprovanteDevolucao;
import circulacao.Comprovantes.ComprovanteEmprestimo;
import circulacao.Comprovantes.ComprovanteRenovacao;
import circulacao.Comprovantes.ComprovanteReserva;
import circulacao.Comprovantes.ComprovanteSituacaoLivro;
import config.Relogio;
import devolucao.EventoDevolucao;
import devolucao.FilaDeInspecao;
import devolucao.PublicadorDevolucao;
import dominio.RegrasCirculacao;
import dominio.livro.Livro;
import dominio.movimentacao.Devolucao;
import dominio.movimentacao.Emprestimo;
import dominio.movimentacao.Reserva;
import dominio.pessoa.Estudante;

import java.util.List;
import java.util.function.Consumer;

// Padrão Facade - papel Facade.
// Entrada única pras operações de circulação. Coordena Relacionamento, Movimentacao, Acervo e Livro
// em operações completas. Cada método roda dentro de UMA unidade de trabalho: se algum passo falhar,
// os anteriores são desfeitos. Devolve comprovantes, nunca coleções.
// Repare que aqui não tem nenhum if sobre o estado do livro: quem decide é o objeto de estado.
public class FachadaCirculacao {
    private final ExecutorTransacional transacao;
    private final PublicadorDevolucao publicador;
    private final FilaDeInspecao filaDeInspecao;
    private final Relogio relogio;

    public FachadaCirculacao(ExecutorTransacional transacao, PublicadorDevolucao publicador,
                             FilaDeInspecao filaDeInspecao, Relogio relogio) {
        this.transacao = transacao;
        this.publicador = publicador;
        this.filaDeInspecao = filaDeInspecao;
        this.relogio = relogio;
    }

    // f1 - solicitar reserva
    public ComprovanteReserva reservar(String idLivro, String idEstudante) {
        return transacao.executar(ctx -> {
            Estudante estudante = ctx.relacionamento().consultarEstudante(idEstudante).orElse(null);
            Livro livro = ctx.acervo().obterLivro(idLivro);           // estado ATUAL (nunca do cache)
            Reserva reserva = livro.reservar(estudante, relogio.hoje());
            ctx.movimentacao().adicionarReserva(reserva);
            ctx.acervo().atualizar(livro);
            return new ComprovanteReserva(reserva.getIdTransacao(), idLivro, livro.getTitulo(), idEstudante,
                    reserva.getValidaAte(), livro.getEstado());
        });
    }

    // f2 - consultar reserva
    public ComprovanteReserva consultarReserva(String idLivro, String idEstudante) {
        return transacao.executar(ctx -> {
            Reserva r = ctx.movimentacao().obterReserva(idLivro, idEstudante);
            Livro livro = ctx.acervo().obterLivro(idLivro);
            return new ComprovanteReserva(r.getIdTransacao(), idLivro, livro.getTitulo(), idEstudante,
                    r.getValidaAte(), livro.getEstado());
        });
    }

    // removerReserva: RESERVADO -> LIVRE
    public ComprovanteSituacaoLivro cancelarReserva(String idLivro, String idEstudante) {
        return transacao.executar(ctx -> {
            Reserva r = ctx.movimentacao().obterReserva(idLivro, idEstudante);
            Livro livro = ctx.acervo().obterLivro(idLivro);
            livro.removerReserva();
            ctx.movimentacao().removerReserva(r);
            ctx.acervo().atualizar(livro);
            return situacao(livro);
        });
    }

    // f3 - efetivar empréstimo. Mesma ordem do diagrama de sequência "Emprestar Livro".
    public ComprovanteEmprestimo efetivarEmprestimo(String idLivro, String idEstudante) {
        return transacao.executar(ctx -> {
            Reserva reserva = ctx.movimentacao().obterReserva(idLivro, idEstudante);      // 2 consultarReserva
            Livro livro = ctx.acervo().obterLivro(idLivro);                               // 3 buscarLivro
            Emprestimo emprestimo = livro.emprestar(reserva, idEstudante, relogio.hoje()); // 4 criarEmprestimo
            ctx.acervo().atualizar(livro);                                                // 5 atualizarEstado
            ctx.movimentacao().removerReserva(reserva);                                   // 6 removerReserva
            ctx.movimentacao().inserirEmprestimo(emprestimo);                             // 7 inserirEmprestimo
            return new ComprovanteEmprestimo(emprestimo.getIdTransacao(), idLivro, livro.getTitulo(), idEstudante,
                    emprestimo.getData(), emprestimo.getDataVencimento(), livro.getEstado());
        });
    }

    // f4 - renovar (a guarda numRenovacoes < 2 fica no estado do livro)
    public ComprovanteRenovacao renovar(String idLivro, String idEstudante) {
        return transacao.executar(ctx -> {
            Emprestimo emprestimo = ctx.movimentacao().obterEmprestimo(idLivro, idEstudante);
            Livro livro = ctx.acervo().obterLivro(idLivro);
            livro.renovar(emprestimo);
            ctx.acervo().atualizar(livro);
            ctx.movimentacao().atualizarEmprestimo(emprestimo);
            return new ComprovanteRenovacao(emprestimo.getIdTransacao(), idLivro, emprestimo.getDataVencimento(),
                    emprestimo.getNumRenovacoes(),
                    RegrasCirculacao.LIMITE_RENOVACOES - emprestimo.getNumRenovacoes(), livro.getEstado());
        });
    }

    // f5 - registrar devolução.
    // Primeiro confirma a transação, DEPOIS publica o evento pros ouvintes
    // (inspeção, multa, indicadores, e-mail). Se um ouvinte falhar, a devolução continua valendo.
    public ComprovanteDevolucao registrarDevolucao(String idLivro, String idEstudante) {
        EventoDevolucao evento = transacao.executar(ctx -> {
            Emprestimo emprestimo = ctx.movimentacao().obterEmprestimo(idLivro, idEstudante);
            Livro livro = ctx.acervo().obterLivro(idLivro);
            Devolucao devolucao = livro.registrarDevolucao(emprestimo, relogio.hoje());
            ctx.movimentacao().inserirDevolucao(devolucao);
            ctx.movimentacao().atualizarEmprestimo(emprestimo);
            ctx.acervo().atualizar(livro);
            Estudante e = ctx.relacionamento().obterEstudante(idEstudante);
            return new EventoDevolucao(devolucao.getIdTransacao(), idLivro, livro.getTitulo(), idEstudante,
                    e.getCpf(), e.getNome(), e.getPessoa().getEmail(), devolucao.getVencimento(),
                    devolucao.getDataDevolucao());
        });
        List<String> falhas = publicador.publicar(evento);    // depois do commit
        String estado = transacao.executar(ctx -> ctx.acervo().obterLivro(idLivro).getEstado());
        return new ComprovanteDevolucao(evento.idDevolucao(), idLivro, idEstudante, evento.vencimento(),
                evento.dataDevolucao(), evento.diasAtraso(), estado, falhas);
    }

    // decisão da inspeção: DEVOLVIDO -> LIVRE (bom estado) ou REPARANDO (danificado)
    public ComprovanteSituacaoLivro inspecionar(String idLivro, boolean bomEstado) {
        ComprovanteSituacaoLivro c = alterarLivro(idLivro, livro -> livro.inspecionar(bomEstado));
        filaDeInspecao.remover(idLivro);
        return c;
    }

    // REPARANDO -> LIVRE
    public ComprovanteSituacaoLivro concluirReparo(String idLivro) {
        return alterarLivro(idLivro, Livro::concluirReparo);
    }

    // REPARANDO -> INCINERADO
    public ComprovanteSituacaoLivro registrarLaudoIrrecuperavel(String idLivro) {
        return alterarLivro(idLivro, Livro::registrarLaudoIrrecuperavel);
    }

    // LIVRE -> REPARANDO
    public ComprovanteSituacaoLivro registrarDano(String idLivro) {
        return alterarLivro(idLivro, Livro::registrarDano);
    }

    private ComprovanteSituacaoLivro alterarLivro(String idLivro, Consumer<Livro> evento) {
        return transacao.executar(ctx -> {
            Livro livro = ctx.acervo().obterLivro(idLivro);
            evento.accept(livro);
            ctx.acervo().atualizar(livro);
            return situacao(livro);
        });
    }

    private static ComprovanteSituacaoLivro situacao(Livro livro) {
        return new ComprovanteSituacaoLivro(livro.getId(), livro.getTitulo(), livro.getEstado());
    }
}
