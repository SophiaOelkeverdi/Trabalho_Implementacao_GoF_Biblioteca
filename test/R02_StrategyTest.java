import dominio.movimentacao.Multa;
import financeiro.politica.PoliticaComTeto;
import financeiro.politica.PoliticaComTolerancia;
import financeiro.politica.PoliticaMulta;
import financeiro.politica.PoliticaOriginal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("R02 - Politicas de calculo de multa (Strategy)")
class R02_StrategyTest {

    @Test
    void R02_cincoDiasDeAtrasoNaPoliticaOriginalCustam5Reais() {
        assertEquals(new BigDecimal("5.00"), new PoliticaOriginal().calcular(5));
    }

    @Test
    void R02_cincoDiasDeAtrasoNaPoliticaComToleranciaCustam3Reais() {
        assertEquals(new BigDecimal("3.00"), new PoliticaComTolerancia().calcular(5));
    }

    @Test
    void R02_semAtrasoOValorEhZeroEmTodasAsPoliticas() {
        for (PoliticaMulta p : List.of(new PoliticaOriginal(), new PoliticaComTeto(), new PoliticaComTolerancia())) {
            assertEquals(new BigDecimal("0.00"), p.calcular(0), p.nome());
        }
    }

    @Test
    void R02_politicaComTetoLimitaA20Reais() {
        assertEquals(new BigDecimal("15.00"), new PoliticaComTeto().calcular(15));
        assertEquals(new BigDecimal("20.00"), new PoliticaComTeto().calcular(45));
    }

    @Test
    void R02_trocaDePoliticaNaoRecalculaMultaJaEmitida() {
        Cenario c = new Cenario();
        // multa emitida com a politica ORIGINAL (5 dias -> R$ 5,00)
        c.emprestar("L1", "E1");
        c.relogio.avancarDias(14 + 5);
        c.app.fachada().registrarDevolucao("L1", "E1");

        c.app.configuracaoAdministrativa().definirPoliticaVigente(PoliticaComTolerancia.NOME);

        // nova multa com a politica nova (5 dias -> R$ 3,00)
        c.emprestar("L2", "E2");
        c.relogio.avancarDias(14 + 5);
        c.app.fachada().registrarDevolucao("L2", "E2");

        List<Multa> multas = c.app.servicoDeMultas().listarMultas();
        assertEquals(2, multas.size());
        Multa antiga = multas.stream().filter(m -> m.getIdEstudante().equals("E1")).findFirst().orElseThrow();
        Multa nova = multas.stream().filter(m -> m.getIdEstudante().equals("E2")).findFirst().orElseThrow();
        assertEquals(new BigDecimal("5.00"), antiga.getValor());
        assertEquals(PoliticaOriginal.NOME, antiga.getPoliticaAplicada());
        assertEquals(new BigDecimal("3.00"), nova.getValor());
        assertEquals(PoliticaComTolerancia.NOME, nova.getPoliticaAplicada());
    }
}
