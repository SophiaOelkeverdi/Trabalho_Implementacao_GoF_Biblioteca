package persistencia.memoria;

import dominio.livro.Livro;
import persistencia.RepositorioLivro;
import persistencia.UnidadeDeTrabalho;

import java.util.List;
import java.util.Optional;

// Padrão Abstract Factory - produto concreto (família em memória)
public class RepositorioLivroMemoria implements RepositorioLivro {
    private final UnidadeDeTrabalhoMemoria unidade;

    RepositorioLivroMemoria(UnidadeDeTrabalhoMemoria unidade) {
        this.unidade = unidade;
    }

    private BaseEmMemoria.Tabelas t() {
        unidade.exigirAtiva();
        return unidade.base().tabelas();
    }

    @Override
    public Optional<Livro> buscar(String idLivro) {
        return Optional.ofNullable(t().livros.get(idLivro)).map(RepositorioLivroMemoria::paraLivro);
    }

    @Override
    public List<Livro> listar() {
        return t().livros.values().stream().map(RepositorioLivroMemoria::paraLivro).toList();
    }

    @Override
    public void salvar(Livro l) {
        t().livros.put(l.getId(), new BaseEmMemoria.LivroReg(l.getId(), l.getTitulo(), l.getAutor(), l.getEstado()));
        unidade.base().contarGravacao();
    }

    @Override
    public void remover(String idLivro) {
        t().livros.remove(idLivro);
        unidade.base().contarGravacao();
    }

    @Override
    public UnidadeDeTrabalho unidadeDeTrabalho() {
        return unidade;
    }

    private static Livro paraLivro(BaseEmMemoria.LivroReg r) {
        return Livro.reconstituir(r.id(), r.titulo(), r.autor(), r.estado());
    }
}
