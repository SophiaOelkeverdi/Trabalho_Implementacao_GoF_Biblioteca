package financeiro.politica;

import java.math.BigDecimal;
import java.math.RoundingMode;

// Padrão Strategy - estratégia concreta: os 2 primeiros dias de atraso não são cobrados
public class PoliticaComTolerancia implements PoliticaMulta {
    public static final String NOME = "TOLERANCIA_2_DIAS";
    static final int DIAS_TOLERANCIA = 2;

    @Override
    public String nome() {
        return NOME;
    }

    @Override
    public BigDecimal calcular(long diasAtraso) {
        long diasCobrados = Math.max(0, diasAtraso - DIAS_TOLERANCIA);
        return PoliticaOriginal.VALOR_DIA.multiply(BigDecimal.valueOf(diasCobrados))
                .setScale(2, RoundingMode.HALF_EVEN);
    }
}
