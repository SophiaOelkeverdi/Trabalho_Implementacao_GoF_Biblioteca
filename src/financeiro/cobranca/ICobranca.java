package financeiro.cobranca;

// Padrão Adapter - papel Target (contrato institucional de cobrança).
// O ServicoDeMultas só conhece esta interface. Cada banco tem um adaptador que faz as conversões
// e traduz as notificações de pagamento do banco.
public interface ICobranca {

    RespostaCobranca emitir(SolicitacaoCobranca solicitacao);

    // quem vai receber as notificações de pagamento (já traduzidas)
    void encaminharPagamentosPara(IQuitacao quitacao);
}
