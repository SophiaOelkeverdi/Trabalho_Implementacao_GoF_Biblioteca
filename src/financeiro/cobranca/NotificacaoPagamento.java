package financeiro.cobranca;

import java.math.BigDecimal;
import java.time.LocalDate;

// Notificação de pagamento no formato da instituição (o adaptador do banco que monta)
public record NotificacaoPagamento(String idNotificacao, String idCobranca, BigDecimal valorPago,
                                   SituacaoCobranca situacao, LocalDate dataPagamento) {
}
