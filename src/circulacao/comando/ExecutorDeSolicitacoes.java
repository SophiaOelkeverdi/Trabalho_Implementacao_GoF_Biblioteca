package circulacao.comando;

import circulacao.FachadaCirculacao;

import java.util.HashMap;
import java.util.Map;

// Padrão Command - papel Invoker.
// Garante que o mesmo id de solicitação não seja executado duas vezes.
// O método é synchronized: se duas submissões com o mesmo id chegarem juntas, a segunda espera a
// primeira terminar, encontra o id no mapa e só recebe o mesmo comprovante (não executa de novo).
// Se a execução der erro, o id NÃO é guardado, então dá pra tentar de novo.
// Obs: o mapa fica em memória. Com vários servidores teria que ser uma tabela no banco.
public class ExecutorDeSolicitacoes {

    // o que a tela recebe: o comprovante e se a solicitação era repetida
    public record Resultado<R>(R comprovante, boolean repetida) { }

    private final FachadaCirculacao receptor;
    private final Map<String, Object> executadas = new HashMap<>();   // id -> comprovante

    public ExecutorDeSolicitacoes(FachadaCirculacao receptor) {
        this.receptor = receptor;
    }

    @SuppressWarnings("unchecked")
    public synchronized <R> Resultado<R> executar(SolicitacaoCirculacao<R> solicitacao) {
        if (executadas.containsKey(solicitacao.getId())) {
            return new Resultado<>((R) executadas.get(solicitacao.getId()), true);
        }
        R comprovante = solicitacao.executar(receptor);
        executadas.put(solicitacao.getId(), comprovante);
        return new Resultado<>(comprovante, false);
    }

    public synchronized boolean jaExecutada(String idSolicitacao) {
        return executadas.containsKey(idSolicitacao);
    }
}
