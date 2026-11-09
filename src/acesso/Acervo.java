package acesso;

import dominio.RegraDeNegocioException;
import dominio.livro.Livro;
import persistencia.RepositorioLivro;

import java.util.List;
import java.util.Optional;

// Ponto interno de acesso "Acervo" (do Trabalho 02): contém os livros
public class Acervo {
    private final RepositorioLivro livros;

    public Acervo(RepositorioLivro livros) {
        this.livros = livros;
    }

    public Optional<Livro> consultarLivro(String idLivro) {
        return livros.buscar(idLivro);
    }

    public Livro obterLivro(String idLivro) {
        return livros.buscar(idLivro)
                .orElseThrow(() -> new RegraDeNegocioException("Livro " + idLivro + " nao cadastrado"));
    }

    public List<Livro> listar() {
        return livros.listar();
    }

    // atualizarEstado() dos diagramas de sequência
    public void atualizar(Livro livro) {
        livros.salvar(livro);
    }

    public void inserir(Livro livro) {
        livros.salvar(livro);
    }

    public void excluir(String idLivro) {
        livros.remover(idLivro);
    }
}
