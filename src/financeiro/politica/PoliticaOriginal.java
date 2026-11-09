package financeiro.politica;

import java.math.BigDecimal;
import java.math.RoundingMode;

// Padrão Strategy - estratégia concreta: R$ 1,00 por dia
public class PoliticaOriginal implements PoliticaMulta {
    public static final String NOME = "ORIGINAL";
    static final BigDecimal VALOR_DIA = new BigDecimal("1.00");

    @Override
    public String nome() {
        return NOME;
    }

    @Override
    public BigDecimal calcular(long diasAtraso) {
        long dias = Math.max(0, diasAtraso);
        return VALOR_DIA.multiply(BigDecimal.valueOf(dias)).setScale(2, RoundingMode.HALF_EVEN);
    }
}
