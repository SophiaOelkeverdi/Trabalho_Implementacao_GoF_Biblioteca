package financeiro.cobranca;

import externo.BancoBApi;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

// Padrão Adapter - papel Adapter (Banco B).
// Todas as conversões do Banco B ficam aqui:
//   valor:    BigDecimal com 2 casas (7 -> 7.00)
//   CPF:      qualquer formato -> "000.000.000-00"
//   data:     LocalDate <-> "AAAA-MM-DD"
//   ids:      idMulta -> referenciaCliente ; idBoleto -> idCobranca
//   situação: texto ("EM_ABERTO", "LIQUIDADO", "BAIXADO") -> SituacaoCobranca
public class AdaptadorBancoB implements ICobranca {
    private final BancoBApi banco;

    public AdaptadorBancoB(BancoBApi banco) {
        this.banco = banco;
    }

    @Override
    public RespostaCobranca emitir(SolicitacaoCobranca s) {
        BancoBApi.BoletoB boleto = banco.criarBoleto(new BancoBApi.PedidoBoletoB(
                s.valor().setScale(2, RoundingMode.UNNECESSARY),
                formatarCpf(s.cpf()),
                s.idMulta(),
                s.vencimento().toString()));
        return new RespostaCobranca(boleto.idBoleto(), boleto.linhaDigitavel(), traduzirSituacao(boleto.status()));
    }

    @Override
    public void encaminharPagamentosPara(IQuitacao quitacao) {
        banco.assinarWebhook(evento -> quitacao.registrarPagamento(traduzir(evento)));
    }

    // webhook do Banco B -> notificação da instituição
    NotificacaoPagamento traduzir(BancoBApi.WebhookB evento) {
        return new NotificacaoPagamento(
                "B-" + evento.idEvento(),
                evento.idBoleto(),
                new BigDecimal(evento.valorPago()),
                traduzirSituacao(evento.status()),
                LocalDate.parse(evento.pagoEmIso()));
    }

    private static String formatarCpf(String cpf) {
        String d = cpf.replaceAll("\\D", "");
        if (d.length() != 11) {
            throw new IllegalArgumentException("CPF invalido para cobranca: " + cpf);
        }
        return d.substring(0, 3) + "." + d.substring(3, 6) + "." + d.substring(6, 9) + "-" + d.substring(9);
    }

    private static SituacaoCobranca traduzirSituacao(String status) {
        return switch (status) {
            case "EM_ABERTO" -> SituacaoCobranca.REGISTRADA;
            case "LIQUIDADO" -> SituacaoCobranca.PAGA;
            case "BAIXADO" -> SituacaoCobranca.CANCELADA;
            default -> throw new IllegalArgumentException("Banco B: status desconhecido " + status);
        };
    }
}
