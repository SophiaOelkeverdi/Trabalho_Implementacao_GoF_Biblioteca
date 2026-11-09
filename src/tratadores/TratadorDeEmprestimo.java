package tratadores;

import circulacao.Comprovantes.ComprovanteEmprestimo;
import circulacao.Comprovantes.ComprovanteRenovacao;
import circulacao.Comprovantes.ComprovanteReserva;
import circulacao.Comprovantes.ComprovanteSituacaoLivro;
import circulacao.FachadaCirculacao;
import circulacao.comando.ExecutorDeSolicitacoes;
import circulacao.comando.ExecutorDeSolicitacoes.Resultado;
import circulacao.comando.SolicitacaoCirculacao;
import circulacao.comando.SolicitacaoRenovacao;
import circulacao.comando.SolicitacaoReserva;

// Tratador de Empréstimo: implementa IEmprestimo.
// Não tem regra de negócio nem olha o estado do livro: reserva e renovação viram solicitações
// (Command) que vão pro executor; o resto vai direto pra fachada.
public class TratadorDeEmprestimo implements IEmprestimo {
    private final FachadaCirculacao fachada;
    private final ExecutorDeSolicitacoes executor;

    public TratadorDeEmprestimo(FachadaCirculacao fachada, ExecutorDeSolicitacoes executor) {
        this.fachada = fachada;
        this.executor = executor;
    }

    @Override
    public ComprovanteReserva solicitarReserva(String idLivro, String idEstudante) {
        return submeter(SolicitacaoReserva.nova(idEstudante, idLivro, idEstudante)).comprovante();
    }

    @Override
    public ComprovanteReserva consultarReserva(String idLivro, String idEstudante) {
        return fachada.consultarReserva(idLivro, idEstudante);
    }

    @Override
    public ComprovanteSituacaoLivro cancelarReserva(String idLivro, String idEstudante) {
        return fachada.cancelarReserva(idLivro, idEstudante);
    }

    @Override
    public ComprovanteEmprestimo realizarEmprestimo(String idLivro, String idEstudante) {
        return fachada.efetivarEmprestimo(idLivro, idEstudante);
    }

    @Override
    public ComprovanteRenovacao renovarEmprestimo(String idLivro, String idEstudante) {
        return submeter(SolicitacaoRenovacao.nova(idEstudante, idLivro, idEstudante)).comprovante();
    }

    @Override
    public <R> Resultado<R> submeter(SolicitacaoCirculacao<R> solicitacao) {
        return executor.executar(solicitacao);
    }
}
