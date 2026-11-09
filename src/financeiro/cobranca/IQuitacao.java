package financeiro.cobranca;

// Interface IQuitacao do diagrama de componentes: o Financeiro recebe a confirmação do pagamento
public interface IQuitacao {

    // true se quitou agora, false se a notificação foi ignorada (repetida ou desconhecida)
    boolean registrarPagamento(NotificacaoPagamento notificacao);
}
