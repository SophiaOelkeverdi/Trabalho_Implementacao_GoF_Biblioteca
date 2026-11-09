package tratadores;

import circulacao.Comprovantes.ComprovanteDevolucao;
import circulacao.Comprovantes.ComprovanteSituacaoLivro;
import circulacao.FachadaCirculacao;
import devolucao.FilaDeInspecao;
import devolucao.IndicadoresCirculacao;
import financeiro.ServicoDeMultas;

import java.util.List;

// Tratador de Devolução: implementa IDevolucao repassando pra fachada e pro serviço de multas
public class TratadorDeDevolucao implements IDevolucao {
    private final FachadaCirculacao fachada;
    private final ServicoDeMultas multas;
    private final FilaDeInspecao filaDeInspecao;
    private final IndicadoresCirculacao indicadores;

    public TratadorDeDevolucao(FachadaCirculacao fachada, ServicoDeMultas multas, FilaDeInspecao filaDeInspecao,
                               IndicadoresCirculacao indicadores) {
        this.fachada = fachada;
        this.multas = multas;
        this.filaDeInspecao = filaDeInspecao;
        this.indicadores = indicadores;
    }

    @Override
    public ComprovanteDevolucao registrarDevolucao(String idLivro, String idEstudante) {
        return fachada.registrarDevolucao(idLivro, idEstudante);
    }

    @Override
    public String emitirBoletoMulta(String idLivro, String idEstudante) {
        return multas.emitirBoletoMulta(idLivro, idEstudante);
    }

    @Override
    public ComprovanteSituacaoLivro inspecionarLivro(String idLivro, boolean bomEstado) {
        return fachada.inspecionar(idLivro, bomEstado);
    }

    @Override
    public ComprovanteSituacaoLivro concluirReparo(String idLivro) {
        return fachada.concluirReparo(idLivro);
    }

    @Override
    public ComprovanteSituacaoLivro registrarLaudoIrrecuperavel(String idLivro) {
        return fachada.registrarLaudoIrrecuperavel(idLivro);
    }

    @Override
    public List<String> listarMultas() {
        return multas.listarMultas().stream().map(Object::toString).toList();
    }

    public List<String> livrosAguardandoInspecao() {
        return filaDeInspecao.pendentes();
    }

    public String indicadores() {
        IndicadoresCirculacao.Retrato r = indicadores.retrato();
        return "Devolucoes: " + r.totalDevolucoes() + " | atrasadas: " + r.devolucoesAtrasadas()
                + " | dias de atraso acumulados: " + r.diasDeAtrasoAcumulados();
    }
}
