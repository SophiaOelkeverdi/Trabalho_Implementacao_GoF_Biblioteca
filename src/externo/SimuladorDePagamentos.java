package externo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

// Só pra DEMONSTRAÇÃO: faz o papel do estudante pagando o boleto no banco.
// Não faz parte do sistema da biblioteca (o sistema só recebe a notificação pelo adaptador).
public class SimuladorDePagamentos {
    private final BancoAApi bancoA;
    private final BancoBApi bancoB;
    private Runnable reenviarUltima = () -> { };

    public SimuladorDePagamentos(BancoAApi bancoA, BancoBApi bancoB) {
        this.bancoA = bancoA;
        this.bancoB = bancoB;
    }

    // paga no banco que emitiu (dá pra saber pelo formato do id)
    public String pagar(String idCobranca, LocalDate data) {
        if (idCobranca.startsWith("A-")) {
            BancoAApi.AvisoPagamentoA aviso = bancoA.simularPagamento(idCobranca.substring(2),
                    data.format(DateTimeFormatter.BASIC_ISO_DATE));
            reenviarUltima = () -> bancoA.reenviar(aviso);
            return "Banco A enviou aviso " + aviso.sequencialAviso() + " (" + aviso.valorPagoCentavos() + " centavos)";
        }
        BancoBApi.WebhookB evento = bancoB.simularPagamento(idCobranca, data.toString());
        reenviarUltima = () -> bancoB.reenviar(evento);
        return "Banco B enviou webhook " + evento.idEvento() + " (R$ " + evento.valorPago() + ")";
    }

    public String reenviarUltimaNotificacao() {
        reenviarUltima.run();
        return "Banco reenviou a mesma notificacao";
    }
}
