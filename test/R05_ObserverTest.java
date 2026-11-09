import circulacao.Comprovantes.ComprovanteDevolucao;
import devolucao.EventoDevolucao;
import devolucao.IndicadoresCirculacao;
import devolucao.OuvinteDevolucao;
import dominio.movimentacao.Multa;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("R05 - Reacoes independentes a devolucao (Observer)")
class R05_ObserverTest {

    @Test
    void R05_devolucaoAtrasadaAlimentaInspecaoMultaEIndicadores() {
        Cenario c = new Cenario();
        c.emprestar("L1", "E1");
        c.relogio.avancarDias(14 + 5);

        ComprovanteDevolucao comp = c.app.tratadorDeDevolucao().registrarDevolucao("L1", "E1");

        assertEquals(5, comp.diasAtraso());
        assertTrue(c.app.filaDeInspecao().contem("L1"), "inspecao");
        List<Multa> multas = c.app.servicoDeMultas().listarMultas();
        assertEquals(1, multas.size(), "multa");
        assertEquals(new BigDecimal("5.00"), multas.get(0).getValor());
        IndicadoresCirculacao.Retrato r = c.app.indicadores().retrato();
        assertEquals(1, r.devolucoesAtrasadas(), "indicadores");
        assertEquals(5, r.diasDeAtrasoAcumulados());
        assertEquals(1, c.email.enviadas().size(), "e-mail");
    }

    @Test
    void R05_devolucaoNoPrazoNaoGeraMulta() {
        Cenario c = new Cenario();
        c.emprestar("L1", "E1");
        c.relogio.avancarDias(10);

        ComprovanteDevolucao comp = c.app.tratadorDeDevolucao().registrarDevolucao("L1", "E1");

        assertEquals(0, comp.diasAtraso());
        assertTrue(c.app.servicoDeMultas().listarMultas().isEmpty());
        assertEquals(1, c.app.indicadores().retrato().totalDevolucoes());
        assertEquals(0, c.app.indicadores().retrato().devolucoesAtrasadas());
    }

    @Test
    void R05_livroPermaneceDevolvidoAteADecisaoDaInspecao() {
        Cenario c = new Cenario();
        c.emprestar("L1", "E1");
        c.relogio.avancarDias(20);
        c.app.tratadorDeDevolucao().registrarDevolucao("L1", "E1");

        assertEquals("DEVOLVIDO", c.estadoDoLivro("L1"), "nenhum ouvinte muda o estado sozinho");

        assertEquals("LIVRE", c.app.tratadorDeDevolucao().inspecionarLivro("L1", true).estadoLivro());
        assertTrue(c.app.filaDeInspecao().pendentes().isEmpty());
    }

    @Test
    void R05_falhaDeUmOuvinteNaoAfetaOsDemaisNemDesfazADevolucao() {
        Cenario c = new Cenario();
        c.email.definirForaDoAr(true);
        c.emprestar("L1", "E1");
        c.relogio.avancarDias(14 + 3);

        ComprovanteDevolucao comp = c.app.tratadorDeDevolucao().registrarDevolucao("L1", "E1");

        assertEquals(1, comp.falhasNasReacoes().size());
        assertTrue(comp.falhasNasReacoes().get(0).startsWith("email"));
        assertEquals("DEVOLVIDO", c.estadoDoLivro("L1"), "a devolucao foi mantida");
        assertTrue(c.emprestimoAtivo("L1").isEmpty());
        assertEquals(1, c.app.servicoDeMultas().listarMultas().size(), "a multa saiu mesmo assim");
        assertTrue(c.app.filaDeInspecao().contem("L1"));
        assertEquals(1, c.app.indicadores().retrato().devolucoesAtrasadas());
    }

    @Test
    void R05_novoOuvinteEhIncluidoSemAlterarOTratador() {
        Cenario c = new Cenario();
        List<String> recebidos = new ArrayList<>();
        c.app.publicador().registrar(new OuvinteDevolucao() {
            @Override public String nome() { return "auditoria"; }
            @Override public void aoRegistrarDevolucao(EventoDevolucao e) { recebidos.add(e.idLivro()); }
        });
        c.emprestar("L1", "E1");

        c.app.tratadorDeDevolucao().registrarDevolucao("L1", "E1");

        assertEquals(List.of("L1"), recebidos);
    }
}
