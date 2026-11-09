package tratadores;

import circulacao.Comprovantes.ComprovanteEmprestimo;
import circulacao.Comprovantes.ComprovanteRenovacao;
import circulacao.Comprovantes.ComprovanteReserva;
import circulacao.Comprovantes.ComprovanteSituacaoLivro;
import circulacao.comando.ExecutorDeSolicitacoes.Resultado;
import circulacao.comando.SolicitacaoCirculacao;

// Interface IEmprestimo (Trabalho 02), implementada pelo TratadorDeEmprestimo.
// Mudamos o retorno de void pra comprovante, pra tela conseguir mostrar o resultado.
public interface IEmprestimo {

    ComprovanteReserva solicitarReserva(String idLivro, String idEstudante);

    ComprovanteReserva consultarReserva(String idLivro, String idEstudante);

    ComprovanteSituacaoLivro cancelarReserva(String idLivro, String idEstudante);

    ComprovanteEmprestimo realizarEmprestimo(String idLivro, String idEstudante);

    ComprovanteRenovacao renovarEmprestimo(String idLivro, String idEstudante);

    // recebe uma solicitação (Command) criada pela tela e manda pro executor
    <R> Resultado<R> submeter(SolicitacaoCirculacao<R> solicitacao);
}
