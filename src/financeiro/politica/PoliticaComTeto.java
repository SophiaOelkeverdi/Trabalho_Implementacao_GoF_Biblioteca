package financeiro.politica;

import java.math.BigDecimal;
import java.math.RoundingMode;

// Padrão Strategy - estratégia concreta: R$ 1,00 por dia com teto de R$ 20,00
public class PoliticaComTeto implements PoliticaMulta {
    public static final String NOME = "TETO_20";
    static final BigDecimal TETO = new BigDecimal("20.00");

    @Override
    public String nome() {
        return NOME;
    }

    @Override
    public BigDecimal calcular(long diasAtraso) {
        long dias = Math.max(0, diasAtraso);
        BigDecimal valor = PoliticaOriginal.VALOR_DIA.multiply(BigDecimal.valueOf(dias));
        return valor.min(TETO).setScale(2, RoundingMode.HALF_EVEN);
    }
}
