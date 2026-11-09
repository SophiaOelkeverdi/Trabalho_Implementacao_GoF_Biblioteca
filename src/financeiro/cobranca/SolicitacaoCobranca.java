package financeiro.cobranca;

import java.math.BigDecimal;
import java.time.LocalDate;

// Pedido de cobrança no formato da instituição (não depende de banco)
public record SolicitacaoCobranca(String idMulta, String cpf, String nomeSacado, BigDecimal valor,
                                  LocalDate vencimento) {
}
