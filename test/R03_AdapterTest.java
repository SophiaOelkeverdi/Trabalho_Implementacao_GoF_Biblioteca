import dominio.movimentacao.Multa;
import externo.BancoAApi;
import externo.BancoBApi;
import financeiro.cobranca.AdaptadorBancoA;
import financeiro.cobranca.AdaptadorBancoB;
import financeiro.cobranca.NotificacaoPagamento;
import financeiro.cobranca.SituacaoCobranca;
import financeiro.cobranca.SolicitacaoCobranca;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DisplayName("R03 - Integracao com bancos incompativeis (Adapter)")
class R03_AdapterTest {

    @Test
    void R03_mesmaSolicitacaoDe7ReaisGera700CentavosNoBancoAE7_00NoBancoB() {
        SolicitacaoCobranca s = new SolicitacaoCobranca("MUL-1", "111.222.333-44", "Ana Souza",
                new BigDecimal("7.00"), LocalDate.of(2026, 12, 1));
        BancoAApi bancoA = new BancoAApi();
        BancoBApi bancoB = new BancoBApi();

        new AdaptadorBancoA(bancoA).emitir(s);
        new AdaptadorBancoB(bancoB).emitir(s);

        BancoAApi.TituloA titulo = bancoA.titulos().get(0);
        assertEquals(700L, titulo.valorCentavos());
        assertEquals("11122233344", titulo.cpfDigitos());
        assertEquals("20261201", titulo.vencimentoAAAAMMDD());

        BancoBApi.BoletoB boleto = bancoB.boletos().get(0);
        assertEquals(new BigDecimal("7.00"), boleto.valor());
        assertEquals("111.222.333-44", boleto.documentoFormatado());
        assertEquals("2026-12-01", boleto.vencimentoIso());
    }

    /** O MESMO ServicoDeMultas (sem if por banco) produz 700 no A e 7.00 no B: so o adaptador muda. */
    @Test
    void R03_servicoDeMultasSemCondicionaisPorBancoEmiteNosDoisBancos() {
        Cenario comA = new Cenario(Cenario.props("banco", "A"));
        devolverCom7DiasDeAtraso(comA);
        assertEquals(700L, comA.app.bancoA().titulos().get(0).valorCentavos());
        assertEquals(0, comA.app.bancoB().boletos().size());

        Cenario comB = new Cenario(Cenario.props("banco", "B"));
        devolverCom7DiasDeAtraso(comB);
        assertEquals(new BigDecimal("7.00"), comB.app.bancoB().boletos().get(0).valor());
        assertEquals(0, comB.app.bancoA().titulos().size());
    }

    @Test
    void R03_notificacaoRepetidaNaoGeraSegundaQuitacao() {
        Cenario c = new Cenario(Cenario.props("banco", "A"));
        devolverCom7DiasDeAtraso(c);
        Multa multa = c.app.servicoDeMultas().listarMultas().get(0);

        BancoAApi.AvisoPagamentoA aviso = c.app.bancoA().simularPagamento(
                multa.getIdCobranca().substring(2), "20261130");
        assertEquals(Multa.Situacao.QUITADA, c.app.servicoDeMultas().listarMultas().get(0).getSituacao());

        c.app.bancoA().reenviar(aviso);                            // banco reenvia o mesmo aviso
        boolean quitouDeNovo = c.app.servicoDeMultas().registrarPagamento(new NotificacaoPagamento("A-AVISO-1",
                multa.getIdCobranca(), new BigDecimal("7.00"), SituacaoCobranca.PAGA, LocalDate.of(2026, 11, 30)));

        assertFalse(quitouDeNovo, "uma notificacao repetida nao pode quitar de novo");
        assertEquals(1, c.app.servicoDeMultas().listarMultas().size());
        assertEquals(Multa.Situacao.QUITADA, c.app.servicoDeMultas().listarMultas().get(0).getSituacao());
    }

    @Test
    void R03_notificacaoDoBancoBTambemEhTraduzidaPeloAdaptador() {
        Cenario c = new Cenario(Cenario.props("banco", "B"));
        devolverCom7DiasDeAtraso(c);
        Multa multa = c.app.servicoDeMultas().listarMultas().get(0);

        c.app.bancoB().simularPagamento(multa.getIdCobranca(), "2026-11-30");

        assertEquals(Multa.Situacao.QUITADA, c.app.servicoDeMultas().listarMultas().get(0).getSituacao());
    }

    private static void devolverCom7DiasDeAtraso(Cenario c) {
        c.emprestar("L1", "E1");
        c.relogio.avancarDias(14 + 7);
        c.app.fachada().registrarDevolucao("L1", "E1");
    }
}
