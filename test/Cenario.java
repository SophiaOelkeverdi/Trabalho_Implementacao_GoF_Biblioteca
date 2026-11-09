import config.Aplicacao;
import devolucao.ServicoEmailSimulado;
import dominio.livro.Livro;
import dominio.movimentacao.Emprestimo;
import dominio.movimentacao.Reserva;
import config.Relogio;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Properties;

/**
 * Monta a aplicacao completa no perfil de TESTE (familia em memoria), com data fixa,
 * uma estudante (E1 - Ana), um segundo estudante (E2 - Bruno) e cinco livros (L1..L5).
 */
public class Cenario {
    public static final LocalDate HOJE = LocalDate.of(2026, 11, 2);
    public static final String CPF_ANA = "11122233344";
    public static final String CPF_BRUNO = "55566677788";

    public final Relogio relogio = new Relogio(Clock.fixed(HOJE.atStartOfDay().toInstant(ZoneOffset.UTC),
            ZoneOffset.UTC));
    public final ServicoEmailSimulado email = new ServicoEmailSimulado();
    public final Aplicacao app;

    public Cenario() {
        this(new Properties());
    }

    public Cenario(Properties extras) {
        Properties p = new Properties();
        p.setProperty("perfil", "teste");
        p.setProperty("banco", "A");
        p.setProperty("politica.multa", "ORIGINAL");
        p.setProperty("consulta.cache", "true");
        p.setProperty("consulta.medicao", "true");
        p.putAll(extras);
        app = Aplicacao.montar(p, relogio, email);
        povoar();
    }

    public static Properties props(String... chaveValor) {
        Properties p = new Properties();
        for (int i = 0; i < chaveValor.length; i += 2) {
            p.setProperty(chaveValor[i], chaveValor[i + 1]);
        }
        return p;
    }

    private void povoar() {
        app.tratadorDePessoa().inserirPessoa("Ana", "Souza", CPF_ANA, "ana@pucpr.edu.br");
        app.tratadorDePessoa().inserirPessoa("Bruno", "Lima", CPF_BRUNO, "bruno@pucpr.edu.br");
        app.tratadorDeEstudante().inserirEstudante("E1", CPF_ANA);
        app.tratadorDeEstudante().inserirEstudante("E2", CPF_BRUNO);
        for (int i = 1; i <= 5; i++) {
            app.tratadorDeAcervo().inserirLivro("L" + i, "Livro " + i, "Autor " + i);
        }
    }

    /** Reserva e efetiva o emprestimo do livro para o estudante. */
    public void emprestar(String idLivro, String idEstudante) {
        app.fachada().reservar(idLivro, idEstudante);
        app.fachada().efetivarEmprestimo(idLivro, idEstudante);
    }

    public String estadoDoLivro(String idLivro) {
        return app.tratadorDeAcervo().consultarLivro(idLivro).getEstado();
    }

    public Livro livro(String idLivro) {
        return app.tratadorDeAcervo().consultarLivro(idLivro);
    }

    public Optional<Emprestimo> emprestimoAtivo(String idLivro) {
        return app.transacao().executar(ctx -> ctx.movimentacao().consultarEmprestimoAtivo(idLivro));
    }

    public Optional<Reserva> reservaDoLivro(String idLivro) {
        return app.transacao().executar(ctx -> ctx.movimentacao().consultarReservaDoLivro(idLivro));
    }
}
