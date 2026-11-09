package acesso;

import persistencia.FabricaPersistencia;
import persistencia.UnidadeDeTrabalho;

import java.util.function.Function;

// Roda uma operação inteira dentro de uma unidade de trabalho:
// cria a unidade, cria os 3 repositórios com ela, confirma no final
// e desfaz tudo se qualquer passo der exceção.
public class ExecutorTransacional {
    private final FabricaPersistencia fabrica;

    public ExecutorTransacional(FabricaPersistencia fabrica) {
        this.fabrica = fabrica;
    }

    public <T> T executar(Function<ContextoDados, T> operacao) {
        UnidadeDeTrabalho unidade = fabrica.criarUnidadeDeTrabalho();
        unidade.iniciar();
        try {
            ContextoDados ctx = new ContextoDados(unidade,
                    new Acervo(fabrica.criarRepositorioLivro(unidade)),
                    new Movimentacao(fabrica.criarRepositorioMovimentacao(unidade)),
                    new Relacionamento(fabrica.criarRepositorioRelacionamento(unidade)));
            T resultado = operacao.apply(ctx);
            unidade.confirmar();
            return resultado;
        } catch (RuntimeException | Error e) {
            unidade.desfazer();
            throw e;
        }
    }

    public FabricaPersistencia fabrica() {
        return fabrica;
    }
}
