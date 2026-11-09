package externo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

// SIMULAÇÃO do Banco B (sistema externo). No padrão Adapter é o Adaptee.
// API diferente do Banco A e da nossa:
//  - valor DECIMAL em reais com 2 casas (7.00)
//  - documento formatado "000.000.000-00"
//  - vencimento ISO "AAAA-MM-DD"
//  - situação em texto ("EM_ABERTO", "LIQUIDADO", "BAIXADO")
//  - avisa pagamento por "webhook" (WebhookB)
public class BancoBApi {

    public record PedidoBoletoB(BigDecimal valor, String documentoFormatado, String referenciaCliente,
                                String vencimentoIso) { }

    public record BoletoB(String idBoleto, BigDecimal valor, String documentoFormatado, String referenciaCliente,
                          String vencimentoIso, String status, String linhaDigitavel) { }

    public record WebhookB(String idEvento, String idBoleto, String valorPago, String status, String pagoEmIso) { }

    private final Map<String, BoletoB> boletos = new LinkedHashMap<>();
    private final List<Consumer<WebhookB>> webhooks = new ArrayList<>();

    public synchronized BoletoB criarBoleto(PedidoBoletoB pedido) {
        if (!pedido.documentoFormatado().matches("\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}")) {
            throw new IllegalArgumentException("Banco B: documento deve estar no formato 000.000.000-00");
        }
        if (pedido.valor().scale() != 2) {
            throw new IllegalArgumentException("Banco B: valor deve ter exatamente 2 casas decimais");
        }
        String id = "B-" + UUID.randomUUID().toString().substring(0, 8);
        BoletoB b = new BoletoB(id, pedido.valor(), pedido.documentoFormatado(), pedido.referenciaCliente(),
                pedido.vencimentoIso(), "EM_ABERTO", "23790." + id.substring(2) + " 0000 " + pedido.valor());
        boletos.put(id, b);
        return b;
    }

    public synchronized void assinarWebhook(Consumer<WebhookB> destino) {
        webhooks.add(destino);
    }

    public synchronized WebhookB simularPagamento(String idBoleto, String pagoEmIso) {
        BoletoB b = boletos.get(idBoleto);
        if (b == null) {
            throw new IllegalArgumentException("Banco B: boleto inexistente " + idBoleto);
        }
        boletos.put(idBoleto, new BoletoB(b.idBoleto(), b.valor(), b.documentoFormatado(), b.referenciaCliente(),
                b.vencimentoIso(), "LIQUIDADO", b.linhaDigitavel()));
        WebhookB evento = new WebhookB("EVT-" + UUID.randomUUID().toString().substring(0, 8), idBoleto,
                b.valor().toPlainString(), "LIQUIDADO", pagoEmIso);
        reenviar(evento);
        return evento;
    }

    public synchronized void reenviar(WebhookB evento) {
        webhooks.forEach(w -> w.accept(evento));
    }

    public synchronized List<BoletoB> boletos() {
        return List.copyOf(boletos.values());
    }
}
