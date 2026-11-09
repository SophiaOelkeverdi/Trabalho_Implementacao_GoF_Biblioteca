package persistencia;

// Padrão Abstract Factory - papel AbstractFactory.
// Cria uma família compatível: unidade de trabalho + repositórios de Livro, Movimentacao e
// Relacionamento. Cada fábrica concreta só aceita unidade de trabalho da própria família,
// então não dá pra misturar repositório em memória com repositório MySQL.
// A fábrica é escolhida uma vez só, pelo perfil (ver PerfilExecucao).
public interface FabricaPersistencia {

    String familia();

    UnidadeDeTrabalho criarUnidadeDeTrabalho();

    RepositorioLivro criarRepositorioLivro(UnidadeDeTrabalho unidade);

    RepositorioMovimentacao criarRepositorioMovimentacao(UnidadeDeTrabalho unidade);

    RepositorioRelacionamento criarRepositorioRelacionamento(UnidadeDeTrabalho unidade);
}
