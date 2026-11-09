package dominio.movimentacao;

import java.math.BigDecimal;
import java.time.LocalDate;

// A multa guarda o valor e o nome da política usados na emissão.
// Por isso trocar a política depois não muda multas que já foram emitidas.
public class Multa {
    public enum Situacao { EMITIDA, QUITADA }

    private final String id;
    private final String idDevolucao;
    private final String idEstudante;
    private final long diasAtraso;
    private final BigDecimal valor;
    private final String politicaAplicada;
    private final LocalDate emitidaEm;
    private String idCobranca;
    private Situacao situacao;

    public Multa(String id, String idDevolucao, String idEstudante, long diasAtraso, BigDecimal valor,
                 String politicaAplicada, LocalDate emitidaEm, String idCobranca, Situacao situacao) {
        this.id = id;
        this.idDevolucao = idDevolucao;
        this.idEstudante = idEstudante;
        this.diasAtraso = diasAtraso;
        this.valor = valor;
        this.politicaAplicada = politicaAplicada;
        this.emitidaEm = emitidaEm;
        this.idCobranca = idCobranca;
        this.situacao = situacao;
    }

    public void vincularCobranca(String idCobranca) { this.idCobranca = idCobranca; }

    // retorna false se já estava quitada (notificação repetida do banco)
    public boolean quitar() {
        if (situacao == Situacao.QUITADA) {
            return false;
        }
        situacao = Situacao.QUITADA;
        return true;
    }

    public String getId() { return id; }
    public String getIdDevolucao() { return idDevolucao; }
    public String getIdEstudante() { return idEstudante; }
    public long getDiasAtraso() { return diasAtraso; }
    public BigDecimal getValor() { return valor; }
    public String getPoliticaAplicada() { return politicaAplicada; }
    public LocalDate getEmitidaEm() { return emitidaEm; }
    public String getIdCobranca() { return idCobranca; }
    public Situacao getSituacao() { return situacao; }

    @Override
    public String toString() {
        return "Multa " + id + " R$ " + valor + " (" + diasAtraso + " dias, politica " + politicaAplicada + ") "
                + situacao + (idCobranca != null ? " cobranca=" + idCobranca : "");
    }
}
