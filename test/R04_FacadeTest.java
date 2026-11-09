import acesso.ExecutorTransacional;
import circulacao.Comprovantes.ComprovanteEmprestimo;
import circulacao.FachadaCirculacao;
import config.Relogio;
import devolucao.FilaDeInspecao;
import devolucao.PublicadorDevolucao;
import dominio.RegraDeNegocioException;
import dominio.livro.Livro;
import dominio.movimentacao.Devolucao;
import dominio.movimentacao.Emprestimo;
import dominio.movimentacao.Multa;
import dominio.movimentacao.Reserva;
import dominio.pessoa.Estudante;
import dominio.pessoa.Pessoa;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import persistencia.FabricaPersistencia;
import persistencia.PersistenciaException;
import persistencia.RepositorioLivro;
import persistencia.RepositorioMovimentacao;
import persistencia.RepositorioRelacionamento;
import persistencia.UnidadeDeTrabalho;
import persistencia.memoria.FabricaPersistenciaMemoria;

import java.lang.reflect.RecordComponent;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("R04 - Entrada unica para as operacoes de circulacao (Facade)")
class R04_FacadeTest {

    @Test
    void R04_efetivarEmprestimoValidaReservaRemoveReservaCriaEmprestimoEAtualizaLivro() {
        Cenario c = new Cenario();
        c.app.fachada().reservar("L1", "E1");
        assertTrue(c.reservaDoLivro("L1").isPresent());

        ComprovanteEmprestimo comp = c.app.tratadorDeEmprestimo().realizarEmprestimo("L1", "E1");

        assertTrue(c.reservaDoLivro("L1").isEmpty(), "a reserva tem que ser removida");
        assertEquals(comp.idEmprestimo(), c.emprestimoAtivo("L1").orElseThrow().getIdTransacao());
        assertEquals("EMPRESTADO", c.estadoDoLivro("L1"));
        assertEquals("EMPRESTADO", comp.estadoLivro());
        assertEquals(Cenario.HOJE.plusDays(14), comp.vencimento());
        // o comprovante só tem dados simples, nenhuma coleção
        for (RecordComponent rc : ComprovanteEmprestimo.class.getRecordComponents()) {
            assertFalse(Collection.class.isAssignableFrom(rc.getType()) || Map.class.isAssignableFrom(rc.getType()),
                    rc.getName());
        }
    }

    @Test
    void R04_emprestimoSemReservaDoEstudanteEhRecusadoSemAlteracoes() {
        Cenario c = new Cenario();
        c.app.fachada().reservar("L1", "E1");

        assertThrows(RegraDeNegocioException.class, () -> c.app.fachada().efetivarEmprestimo("L1", "E2"));

        assertEquals("RESERVADO", c.estadoDoLivro("L1"));
        assertTrue(c.reservaDoLivro("L1").isPresent());
        assertTrue(c.emprestimoAtivo("L1").isEmpty());
    }

    // Se um passo falhar no meio (gravar o empréstimo), os passos anteriores são desfeitos.
    @Test
    void R04_falhaNoMeioDaOperacaoDesfazTodosOsPassos() {
        FabricaQueFalha fabrica = new FabricaQueFalha();
        ExecutorTransacional tx = new ExecutorTransacional(fabrica);
        FachadaCirculacao fachada = new FachadaCirculacao(tx, new PublicadorDevolucao(), new FilaDeInspecao(),
                new Relogio());
        tx.executar(ctx -> {
            Pessoa ana = new Pessoa("Ana", "Souza", Cenario.CPF_ANA, "ana@x.br");
            ctx.relacionamento().inserirPessoa(ana);
            ctx.relacionamento().inserirEstudante(new Estudante("E1", ana));
            ctx.acervo().inserir(new Livro("L1", "Livro 1", "Autor"));
            return null;
        });
        fachada.reservar("L1", "E1");

        fabrica.falharAoGravarEmprestimo = true;
        assertThrows(PersistenciaException.class, () -> fachada.efetivarEmprestimo("L1", "E1"));

        tx.executar(ctx -> {
            assertEquals("RESERVADO", ctx.acervo().obterLivro("L1").getEstado(), "estado do livro voltou");
            assertTrue(ctx.movimentacao().consultarReservaDoLivro("L1").isPresent(), "reserva voltou");
            assertTrue(ctx.movimentacao().consultarEmprestimoAtivo("L1").isEmpty(), "nenhum emprestimo criado");
            return null;
        });
    }

    // Fábrica de teste: usa a família em memória, mas o repositório de movimentação pode falhar
    static class FabricaQueFalha implements FabricaPersistencia {
        final FabricaPersistenciaMemoria real = new FabricaPersistenciaMemoria();
        boolean falharAoGravarEmprestimo;

        @Override public String familia() { return real.familia(); }
        @Override public UnidadeDeTrabalho criarUnidadeDeTrabalho() { return real.criarUnidadeDeTrabalho(); }
        @Override public RepositorioLivro criarRepositorioLivro(UnidadeDeTrabalho u) {
            return real.criarRepositorioLivro(u);
        }
        @Override public RepositorioRelacionamento criarRepositorioRelacionamento(UnidadeDeTrabalho u) {
            return real.criarRepositorioRelacionamento(u);
        }
        @Override public RepositorioMovimentacao criarRepositorioMovimentacao(UnidadeDeTrabalho u) {
            return new MovimentacaoQueFalha(real.criarRepositorioMovimentacao(u), this);
        }
    }

    // repassa tudo pro repositório de verdade, menos salvarEmprestimo quando a falha está ligada
    static class MovimentacaoQueFalha implements RepositorioMovimentacao {
        private final RepositorioMovimentacao real;
        private final FabricaQueFalha fabrica;

        MovimentacaoQueFalha(RepositorioMovimentacao real, FabricaQueFalha fabrica) {
            this.real = real;
            this.fabrica = fabrica;
        }

        @Override public void salvarEmprestimo(Emprestimo e) {
            if (fabrica.falharAoGravarEmprestimo) {
                throw new PersistenciaException("falha simulada ao gravar emprestimo");
            }
            real.salvarEmprestimo(e);
        }

        @Override public void salvarReserva(Reserva r) { real.salvarReserva(r); }
        @Override public Optional<Reserva> buscarReservaDoLivro(String id) { return real.buscarReservaDoLivro(id); }
        @Override public void removerReserva(String id) { real.removerReserva(id); }
        @Override public Optional<Emprestimo> buscarEmprestimoAtivo(String id) { return real.buscarEmprestimoAtivo(id); }
        @Override public void salvarDevolucao(Devolucao d) { real.salvarDevolucao(d); }
        @Override public Optional<Devolucao> buscarUltimaDevolucao(String l, String e) {
            return real.buscarUltimaDevolucao(l, e);
        }
        @Override public void salvarMulta(Multa m) { real.salvarMulta(m); }
        @Override public Optional<Multa> buscarMultaPorDevolucao(String id) { return real.buscarMultaPorDevolucao(id); }
        @Override public Optional<Multa> buscarMultaPorCobranca(String id) { return real.buscarMultaPorCobranca(id); }
        @Override public List<Multa> listarMultas() { return real.listarMultas(); }
        @Override public UnidadeDeTrabalho unidadeDeTrabalho() { return real.unidadeDeTrabalho(); }
    }
}
