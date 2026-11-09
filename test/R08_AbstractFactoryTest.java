import acesso.ExecutorTransacional;
import config.Aplicacao;
import devolucao.ServicoEmailSimulado;
import dominio.livro.Livro;
import config.Relogio;
import persistencia.FabricaPersistencia;
import persistencia.FamiliaIncompativelException;
import persistencia.PerfilExecucao;
import persistencia.PersistenciaException;
import persistencia.RepositorioLivro;
import persistencia.RepositorioMovimentacao;
import persistencia.RepositorioRelacionamento;
import persistencia.UnidadeDeTrabalho;
import persistencia.jdbc.FabricaPersistenciaJdbc;
import persistencia.jdbc.RepositorioLivroJdbc;
import persistencia.jdbc.RepositorioMovimentacaoJdbc;
import persistencia.jdbc.RepositorioRelacionamentoJdbc;
import persistencia.jdbc.UnidadeDeTrabalhoJdbc;
import persistencia.memoria.FabricaPersistenciaMemoria;
import persistencia.memoria.RepositorioLivroMemoria;
import persistencia.memoria.RepositorioMovimentacaoMemoria;
import persistencia.memoria.RepositorioRelacionamentoMemoria;
import persistencia.memoria.UnidadeDeTrabalhoMemoria;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("R08 - Familias compativeis de acesso aos dados (Abstract Factory)")
class R08_AbstractFactoryTest {

    @Test
    void R08_configuracaoDeTesteForneceApenasObjetosEmMemoria() {
        FabricaPersistencia f = PerfilExecucao.escolherFabrica(Cenario.props("perfil", "teste"));
        UnidadeDeTrabalho u = f.criarUnidadeDeTrabalho();

        assertInstanceOf(FabricaPersistenciaMemoria.class, f);
        assertInstanceOf(UnidadeDeTrabalhoMemoria.class, u);
        assertInstanceOf(RepositorioLivroMemoria.class, f.criarRepositorioLivro(u));
        assertInstanceOf(RepositorioMovimentacaoMemoria.class, f.criarRepositorioMovimentacao(u));
        assertInstanceOf(RepositorioRelacionamentoMemoria.class, f.criarRepositorioRelacionamento(u));
    }

    @Test
    void R08_configuracaoDeProducaoForneceRepositoriosMySqlQueCompartilhamAMesmaUnidadeDeTrabalho() {
        Properties producao = Cenario.props("perfil", "producao",
                "jdbc.url", "jdbc:mysql://localhost:3306/biblioteca",
                "jdbc.usuario", "u", "jdbc.senha", "s",
                "jdbc.criarEsquema", "false");      // nao precisa de servidor: a conexao so abre em iniciar()
        FabricaPersistencia f = PerfilExecucao.escolherFabrica(producao);
        UnidadeDeTrabalho u = f.criarUnidadeDeTrabalho();

        RepositorioLivro livros = f.criarRepositorioLivro(u);
        RepositorioMovimentacao mov = f.criarRepositorioMovimentacao(u);
        RepositorioRelacionamento rel = f.criarRepositorioRelacionamento(u);

        assertInstanceOf(FabricaPersistenciaJdbc.class, f);
        assertInstanceOf(UnidadeDeTrabalhoJdbc.class, u);
        assertInstanceOf(RepositorioLivroJdbc.class, livros);
        assertInstanceOf(RepositorioMovimentacaoJdbc.class, mov);
        assertInstanceOf(RepositorioRelacionamentoJdbc.class, rel);
        assertSame(u, livros.unidadeDeTrabalho());
        assertSame(u, mov.unidadeDeTrabalho());
        assertSame(u, rel.unidadeDeTrabalho());
    }

    @Test
    void R08_naoEhPossivelMisturarRepositorioRelacionalComUnidadeEmMemoria() {
        FabricaPersistencia memoria = new FabricaPersistenciaMemoria();
        FabricaPersistencia mysql = new FabricaPersistenciaJdbc("jdbc:mysql://localhost/x", "u", "s");

        UnidadeDeTrabalho unidadeEmMemoria = memoria.criarUnidadeDeTrabalho();

        assertThrows(FamiliaIncompativelException.class, () -> mysql.criarRepositorioLivro(unidadeEmMemoria));
        assertThrows(FamiliaIncompativelException.class,
                () -> memoria.criarRepositorioLivro(mysql.criarUnidadeDeTrabalho()));
    }

    @Test
    void R08_oracleContinuaReplicaENaoRecebeEscritas() {
        assertThrows(PersistenciaException.class,
                () -> new FabricaPersistenciaJdbc("jdbc:oracle:thin:@bd02:1521/XE", "u", "s"));
    }

    /** Executa a familia relacional de verdade (H2 em modo MySQL), inclusive o rollback. */
    @Test
    void R08_familiaRelacionalGravaReconstituiEstadoEDesfazTransacao() {
        Properties producao = Cenario.props("perfil", "producao",
                "jdbc.url", "jdbc:h2:mem:r08;MODE=MySQL;DB_CLOSE_DELAY=-1",
                "jdbc.usuario", "sa", "jdbc.senha", "",
                "banco", "A", "politica.multa", "ORIGINAL");
        Aplicacao app = Aplicacao.montar(producao, new Relogio(), new ServicoEmailSimulado());
        app.tratadorDePessoa().inserirPessoa("Ana", "Souza", Cenario.CPF_ANA, "ana@x.br");
        app.tratadorDeEstudante().inserirEstudante("E1", Cenario.CPF_ANA);
        app.tratadorDeAcervo().inserirLivro("L1", "Livro 1", "Autor");

        app.fachada().reservar("L1", "E1");
        app.fachada().efetivarEmprestimo("L1", "E1");
        app.fachada().renovar("L1", "E1");

        assertEquals("RENOVADO", app.tratadorDeAcervo().consultarLivro("L1").getEstado());

        ExecutorTransacional tx = app.transacao();
        assertThrows(IllegalStateException.class, () -> tx.executar(ctx -> {
            ctx.acervo().inserir(new Livro("L2", "Nao deve ficar", "x"));
            throw new IllegalStateException("falha depois de gravar");
        }));
        assertTrue(tx.executar(ctx -> ctx.acervo().consultarLivro("L2")).isEmpty(), "rollback no MySQL/H2");
    }
}
