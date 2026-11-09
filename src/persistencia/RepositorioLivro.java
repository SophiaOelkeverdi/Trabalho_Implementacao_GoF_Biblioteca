package persistencia;

import dominio.livro.Livro;

import java.util.List;
import java.util.Optional;

// Padrão Abstract Factory - produto abstrato (repositório de Livro)
public interface RepositorioLivro {

    Optional<Livro> buscar(String idLivro);

    List<Livro> listar();

    // insere ou atualiza; o estado é gravado pelo nome (Livro.getEstado())
    void salvar(Livro livro);

    void remover(String idLivro);

    UnidadeDeTrabalho unidadeDeTrabalho();
}
