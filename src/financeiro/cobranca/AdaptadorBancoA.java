package financeiro.cobranca;

import externo.BancoAApi;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

// Padrão Adapter - papel Adapter (Banco A).
// Todas as conversões do Banco A ficam aqui:
//   valor:    7.00 reais       -> 700 centavos (long)
//   CPF:      111.222.333-44   -> 11122233344
//   data:     LocalDate        <-> "AAAAMMDD"
//   ids:      idMulta -> seuNumero ; nossoNumero -> idCobranca "A-<nossoNumero>"
//   situação: código numérico  -> SituacaoCobranca
public class AdaptadorBancoA implements ICobranca {
    private static final DateTimeFormatter AAAAMMDD = DateTimeFormatter.BASIC_ISO_DATE;
    private static final String PREFIXO = "A-";

    private final BancoAApi banco;

    public AdaptadorBancoA(BancoAApi banco) {
        this.banco = banco;
    }

    @Override
    public RespostaCobranca emitir(SolicitacaoCobranca s) {
        long centavos = s.valor().movePointRight(2).longValueExact();
        String cpfDigitos = s.cpf().replaceAll("\\D", "");
        BancoAApi.TituloA titulo = banco.registrarTitulo(centavos, cpfDigitos, s.idMulta(),
                s.vencimento().format(AAAAMMDD));
        return new RespostaCobranca(PREFIXO + titulo.nossoNumero(),
                "34191." + titulo.nossoNumero() + " " + titulo.valorCentavos(),
                traduzirSituacao(titulo.codigoSituacao()));
    }

    @Override
    public void encaminharPagamentosPara(IQuitacao quitacao) {
        banco.registrarCallback(aviso -> quitacao.registrarPagamento(traduzir(aviso)));
    }

    // aviso do Banco A -> notificação da instituição
    NotificacaoPagamento traduzir(BancoAApi.AvisoPagamentoA aviso) {
        return new NotificacaoPagamento(
                "A-AVISO-" + aviso.sequencialAviso(),
                PREFIXO + aviso.nossoNumero(),
                BigDecimal.valueOf(aviso.valorPagoCentavos(), 2),
                traduzirSituacao(aviso.codigoSituacao()),
                LocalDate.parse(aviso.dataPagamentoAAAAMMDD(), AAAAMMDD));
    }

    private static SituacaoCobranca traduzirSituacao(int codigo) {
        return switch (codigo) {
            case BancoAApi.EM_ABERTO -> SituacaoCobranca.REGISTRADA;
            case BancoAApi.LIQUIDADO -> SituacaoCobranca.PAGA;
            case BancoAApi.BAIXADO -> SituacaoCobranca.CANCELADA;
            default -> throw new IllegalArgumentException("Banco A: codigo de situacao desconhecido " + codigo);
        };
    }
}
