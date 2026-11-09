package persistencia.memoria;

import persistencia.FabricaPersistencia;
import persistencia.FamiliaIncompativelException;
import persistencia.RepositorioLivro;
import persistencia.RepositorioMovimentacao;
import persistencia.RepositorioRelacionamento;
import persistencia.UnidadeDeTrabalho;

// Padrão Abstract Factory - fábrica concreta da família EM MEMÓRIA (perfil teste)
public class FabricaPersistenciaMemoria implements FabricaPersistencia {
    public static final String FAMILIA = "memoria";

    private final BaseEmMemoria base = new BaseEmMemoria();

    @Override
    public String familia() {
        return FAMILIA;
    }

    public BaseEmMemoria base() {
        return base;
    }

    @Override
    public UnidadeDeTrabalho criarUnidadeDeTrabalho() {
        return new UnidadeDeTrabalhoMemoria(base);
    }

    @Override
    public RepositorioLivro criarRepositorioLivro(UnidadeDeTrabalho unidade) {
        return new RepositorioLivroMemoria(daFamilia(unidade));
    }

    @Override
    public RepositorioMovimentacao criarRepositorioMovimentacao(UnidadeDeTrabalho unidade) {
        return new RepositorioMovimentacaoMemoria(daFamilia(unidade));
    }

    @Override
    public RepositorioRelacionamento criarRepositorioRelacionamento(UnidadeDeTrabalho unidade) {
        return new RepositorioRelacionamentoMemoria(daFamilia(unidade));
    }

    // não deixa misturar famílias: só aceita unidade em memória desta mesma base
    private UnidadeDeTrabalhoMemoria daFamilia(UnidadeDeTrabalho unidade) {
        if (unidade instanceof UnidadeDeTrabalhoMemoria m && m.base() == base) {
            return m;
        }
        throw new FamiliaIncompativelException(FAMILIA, unidade);
    }
}
