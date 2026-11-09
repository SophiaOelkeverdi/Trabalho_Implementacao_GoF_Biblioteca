package acesso;

import persistencia.UnidadeDeTrabalho;

// Os 3 pontos de acesso de uma operação, todos usando a MESMA unidade de trabalho
public record ContextoDados(UnidadeDeTrabalho unidade, Acervo acervo, Movimentacao movimentacao,
                            Relacionamento relacionamento) {
}
