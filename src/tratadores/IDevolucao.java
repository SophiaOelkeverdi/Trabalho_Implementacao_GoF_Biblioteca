package tratadores;

import circulacao.Comprovantes.ComprovanteDevolucao;
import circulacao.Comprovantes.ComprovanteSituacaoLivro;

import java.util.List;

// Interface IDevolucao (Trabalho 02), implementada pelo TratadorDeDevolucao
public interface IDevolucao {

    ComprovanteDevolucao registrarDevolucao(String idLivro, String idEstudante);

    String emitirBoletoMulta(String idLivro, String idEstudante);

    ComprovanteSituacaoLivro inspecionarLivro(String idLivro, boolean bomEstado);

    ComprovanteSituacaoLivro concluirReparo(String idLivro);

    ComprovanteSituacaoLivro registrarLaudoIrrecuperavel(String idLivro);

    List<String> listarMultas();
}
