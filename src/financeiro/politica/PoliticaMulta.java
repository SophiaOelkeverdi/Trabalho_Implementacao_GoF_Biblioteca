package financeiro.politica;

import java.math.BigDecimal;

// Padrão Strategy - papel Strategy.
// Calcula o valor da multa a partir dos dias de atraso (BigDecimal com 2 casas, nunca double).
// Pra criar uma política nova: implementar esta interface e registrar na Aplicacao.
public interface PoliticaMulta {

    String nome();   // é gravado na multa

    BigDecimal calcular(long diasAtraso);
}
