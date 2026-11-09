package financeiro;

import acesso.ExecutorTransacional;
import config.Relogio;
import dominio.Ids;
import dominio.RegraDeNegocioException;
import dominio.movimentacao.Devolucao;
import dominio.movimentacao.Multa;
import dominio.pessoa.Estudante;
import financeiro.cobranca.ICobranca;
import financeiro.cobranca.IQuitacao;
import financeiro.cobranca.NotificacaoPagamento;
import financeiro.cobranca.RespostaCobranca;
import financeiro.cobranca.SituacaoCobranca;
import financeiro.cobranca.SolicitacaoCobranca;
import financeiro.politica.ConfiguracaoAdministrativa;
import financeiro.politica.PoliticaMulta;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

// Serviço de multas do Financeiro.
// Strategy: é o Context, usa a PoliticaMulta vigente sem saber qual é.
// Adapter: é o Client, usa ICobranca sem saber qual banco está atrás (não tem nenhum if por banco aqui).
// Também implementa IQuitacao pra receber os pagamentos já traduzidos pelo adaptador.
public class ServicoDeMultas implements IQuitacao {
    static final int DIAS_PARA_PAGAR_BOLETO = 10;

    private final ExecutorTransacional transacao;
    private final ConfiguracaoAdministrativa configuracao;
    private final ICobranca cobranca;
    private final Relogio relogio;

    public ServicoDeMultas(ExecutorTransacional transacao, ConfiguracaoAdministrativa configuracao,
                           ICobranca cobranca, Relogio relogio) {
        this.transacao = transacao;
        this.configuracao = configuracao;
        this.cobranca = cobranca;
        this.relogio = relogio;
        cobranca.encaminharPagamentosPara(this);
    }

    // Emite a multa com a política VIGENTE e grava valor + nome da política.
    // Se a devolução já tem multa, devolve a que existe (nunca recalcula).
    public Optional<Multa> emitirMulta(String idDevolucao, String idEstudante, String cpf, String nome,
                                       long diasAtraso) {
        if (diasAtraso <= 0) {
            return Optional.empty();
        }
        PoliticaMulta politica = configuracao.politicaVigente();
        BigDecimal valor = politica.calcular(diasAtraso);
        if (valor.signum() == 0) {
            return Optional.empty();        // ex: atraso dentro da tolerância
        }
        Multa multa = transacao.executar(ctx -> ctx.movimentacao().consultarMultaDaDevolucao(idDevolucao)
                .orElseGet(() -> {
                    Multa nova = new Multa(Ids.novo("MUL"), idDevolucao, idEstudante, diasAtraso, valor,
                            politica.nome(), relogio.hoje(), null, Multa.Situacao.EMITIDA);
                    ctx.movimentacao().salvarMulta(nova);
                    return nova;
                }));
        if (multa.getIdCobranca() == null) {
            emitirBoleto(multa, cpf, nome);
        }
        return Optional.of(multa);
    }

    // A emissão do boleto é igual pra qualquer política e qualquer banco.
    // Se o banco falhar, a multa continua gravada (sem boleto) e dá pra emitir de novo depois.
    private RespostaCobranca emitirBoleto(Multa multa, String cpf, String nome) {
        RespostaCobranca resposta = cobranca.emitir(new SolicitacaoCobranca(multa.getId(), cpf, nome,
                multa.getValor(), relogio.hoje().plusDays(DIAS_PARA_PAGAR_BOLETO)));
        multa.vincularCobranca(resposta.idCobranca());
        transacao.executar(ctx -> {
            ctx.movimentacao().salvarMulta(multa);
            return null;
        });
        return resposta;
    }

    // emitirBoletoMulta (f6): acha a devolução, pega a multa e emite o boleto se ainda não tiver
    public String emitirBoletoMulta(String idLivro, String idEstudante) {
        record Dados(Devolucao devolucao, Optional<Multa> multa, Estudante estudante) { }
        Dados d = transacao.executar(ctx -> {
            Devolucao dev = ctx.movimentacao().obterUltimaDevolucao(idLivro, idEstudante);
            return new Dados(dev, ctx.movimentacao().consultarMultaDaDevolucao(dev.getIdTransacao()),
                    ctx.relacionamento().obterEstudante(idEstudante));
        });
        Multa multa = d.multa().orElseThrow(() ->
                new RegraDeNegocioException("A devolucao " + d.devolucao().getIdTransacao() + " nao gerou multa"));
        if (multa.getIdCobranca() == null) {
            emitirBoleto(multa, d.estudante().getCpf(), d.estudante().getNome());
        }
        return "Boleto " + multa.getIdCobranca() + " - R$ " + multa.getValor() + " (" + multa.getDiasAtraso()
                + " dias, politica " + multa.getPoliticaAplicada() + ") - " + multa.getSituacao()
                + " - enviado para " + d.estudante().getPessoa().getEmail();
    }

    // Chamado pelo adaptador do banco. Se a notificação vier repetida, a multa já está QUITADA
    // e quitar() devolve false, então não tem segunda quitação.
    @Override
    public boolean registrarPagamento(NotificacaoPagamento notificacao) {
        if (notificacao.situacao() != SituacaoCobranca.PAGA) {
            return false;
        }
        return transacao.executar(ctx -> ctx.movimentacao().consultarMultaPorCobranca(notificacao.idCobranca())
                .map(multa -> {
                    boolean quitouAgora = multa.quitar();
                    if (quitouAgora) {
                        ctx.movimentacao().salvarMulta(multa);
                    }
                    return quitouAgora;
                })
                .orElse(false));
    }

    public List<Multa> listarMultas() {
        return transacao.executar(ctx -> ctx.movimentacao().listarMultas());
    }
}
