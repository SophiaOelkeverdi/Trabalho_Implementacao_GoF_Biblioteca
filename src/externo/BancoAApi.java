package externo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

// SIMULAÇÃO do Banco A (sistema externo). No padrão Adapter é o Adaptee.
// API diferente da nossa de propósito:
//  - valor em CENTAVOS (long)
//  - CPF só com dígitos
//  - vencimento como texto "AAAAMMDD"
//  - situação como código (0 = em aberto, 2 = liquidado, 9 = baixado)
//  - avisa pagamento por callback (AvisoPagamentoA)
public class BancoAApi {

    public record TituloA(String nossoNumero, long valorCentavos, String cpfDigitos, String seuNumero,
                          String vencimentoAAAAMMDD, int codigoSituacao) { }

    public record AvisoPagamentoA(long sequencialAviso, String nossoNumero, long valorPagoCentavos,
                                  int codigoSituacao, String dataPagamentoAAAAMMDD) { }

    public static final int EM_ABERTO = 0;
    public static final int LIQUIDADO = 2;
    public static final int BAIXADO = 9;

    private final Map<String, TituloA> titulos = new LinkedHashMap<>();
    private final List<Consumer<AvisoPagamentoA>> ouvintes = new ArrayList<>();
    private long proximoNossoNumero = 100001;
    private long proximoAviso = 1;

    public synchronized TituloA registrarTitulo(long valorCentavos, String cpfDigitos, String seuNumero,
                                                String vencimentoAAAAMMDD) {
        if (!cpfDigitos.matches("\\d{11}")) {
            throw new IllegalArgumentException("Banco A: CPF deve ter 11 digitos sem pontuacao");
        }
        String nossoNumero = String.valueOf(proximoNossoNumero++);
        TituloA t = new TituloA(nossoNumero, valorCentavos, cpfDigitos, seuNumero, vencimentoAAAAMMDD, EM_ABERTO);
        titulos.put(nossoNumero, t);
        return t;
    }

    public synchronized void registrarCallback(Consumer<AvisoPagamentoA> ouvinte) {
        ouvintes.add(ouvinte);
    }

    // simula o estudante pagando: o banco manda um aviso pro callback
    public synchronized AvisoPagamentoA simularPagamento(String nossoNumero, String dataAAAAMMDD) {
        TituloA t = titulos.get(nossoNumero);
        if (t == null) {
            throw new IllegalArgumentException("Banco A: titulo inexistente " + nossoNumero);
        }
        titulos.put(nossoNumero, new TituloA(t.nossoNumero(), t.valorCentavos(), t.cpfDigitos(), t.seuNumero(),
                t.vencimentoAAAAMMDD(), LIQUIDADO));
        AvisoPagamentoA aviso = new AvisoPagamentoA(proximoAviso++, nossoNumero, t.valorCentavos(), LIQUIDADO,
                dataAAAAMMDD);
        reenviar(aviso);
        return aviso;
    }

    // banco mandando o mesmo aviso de novo (acontece com webhook de verdade)
    public synchronized void reenviar(AvisoPagamentoA aviso) {
        ouvintes.forEach(o -> o.accept(aviso));
    }

    public synchronized List<TituloA> titulos() {
        return List.copyOf(titulos.values());
    }
}
