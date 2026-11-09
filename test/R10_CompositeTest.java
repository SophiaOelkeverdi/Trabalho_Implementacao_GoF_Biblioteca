import dominio.RegraDeNegocioException;
import dominio.movimentacao.Emprestimo;
import organizacao.GrupoAcervo;
import organizacao.ItemLivro;
import organizacao.NoAcervo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("R10 - Organizacao hierarquica do acervo (Composite)")
class R10_CompositeTest {

    @Test
    void R10_unidadeComDuasSecoesDeTresEDoisLivrosInformaTotalDeCinco() {
        GrupoAcervo unidade = new GrupoAcervo("Central", GrupoAcervo.Tipo.UNIDADE);
        GrupoAcervo computacao = new GrupoAcervo("Computacao", GrupoAcervo.Tipo.SECAO);
        GrupoAcervo engenharia = new GrupoAcervo("Engenharia", GrupoAcervo.Tipo.SECAO);
        unidade.adicionar(computacao).adicionar(engenharia);
        computacao.adicionarLivro("L1").adicionarLivro("L2").adicionarLivro("L3");
        engenharia.adicionarLivro("L4").adicionarLivro("L5");

        assertEquals(5, unidade.contarLivros());
        assertEquals(3, computacao.contarLivros());
        assertEquals(List.of("L1", "L2", "L3", "L4", "L5"), unidade.listarLivros());
    }

    @Test
    void R10_moverLivroEntreSecoesNaoAlteraIdEstadoNemStatusDeEmprestimo() {
        Cenario c = new Cenario();
        c.emprestar("L1", "E1");
        Emprestimo antes = c.emprestimoAtivo("L1").orElseThrow();
        var org = c.app.tratadorDeOrganizacao();
        org.criarGrupo("Central", "Computacao", GrupoAcervo.Tipo.SECAO);
        org.criarGrupo("Central", "Engenharia", GrupoAcervo.Tipo.SECAO);
        org.incluirLivro("Computacao", "L1");

        org.moverLivro("L1", "Engenharia");

        assertEquals(0, org.contarLivros("Computacao"));
        assertEquals(1, org.contarLivros("Engenharia"));
        assertEquals("L1", c.livro("L1").getId());
        assertEquals("EMPRESTADO", c.estadoDoLivro("L1"));
        Emprestimo depois = c.emprestimoAtivo("L1").orElseThrow();
        assertEquals(antes.getIdTransacao(), depois.getIdTransacao());
        assertEquals(antes.getDataVencimento(), depois.getDataVencimento());
        assertTrue(depois.isAtivo());
    }

    @Test
    void R10_grupoRecusaCiclos() {
        GrupoAcervo unidade = new GrupoAcervo("Central", GrupoAcervo.Tipo.UNIDADE);
        GrupoAcervo secao = new GrupoAcervo("Computacao", GrupoAcervo.Tipo.SECAO);
        GrupoAcervo subsecao = new GrupoAcervo("Algoritmos", GrupoAcervo.Tipo.SUBSECAO);
        unidade.adicionar(secao);
        secao.adicionar(subsecao);

        assertThrows(RegraDeNegocioException.class, () -> subsecao.adicionar(unidade));
        assertThrows(RegraDeNegocioException.class, () -> secao.adicionar(secao));
    }

    @Test
    void R10_grupoRecusaLivroQueJaPertenceAOutroGrupo() {
        GrupoAcervo unidade = new GrupoAcervo("Central", GrupoAcervo.Tipo.UNIDADE);
        GrupoAcervo a = new GrupoAcervo("A", GrupoAcervo.Tipo.SECAO);
        GrupoAcervo b = new GrupoAcervo("B", GrupoAcervo.Tipo.SECAO);
        unidade.adicionar(a).adicionar(b);
        a.adicionarLivro("L1");

        assertThrows(RegraDeNegocioException.class, () -> b.adicionarLivro("L1"));
        ItemLivro item = a.localizar("L1").orElseThrow();
        assertThrows(RegraDeNegocioException.class, () -> b.adicionar(item));
        assertEquals(1, unidade.contarLivros());
    }

    @Test
    void R10_gruposEItensRespondemAsMesmasOperacoes() {
        NoAcervo item = new ItemLivro("L7");
        NoAcervo grupo = new GrupoAcervo("Vazio", GrupoAcervo.Tipo.SUBSECAO);
        assertEquals(1, item.contarLivros());
        assertEquals(List.of("L7"), item.listarLivros());
        assertEquals(0, grupo.contarLivros());
        assertEquals(List.of(), grupo.listarLivros());
    }
}
