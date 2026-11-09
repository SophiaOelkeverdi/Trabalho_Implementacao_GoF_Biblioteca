package persistencia.jdbc;

import dominio.livro.Livro;
import persistencia.RepositorioLivro;
import persistencia.UnidadeDeTrabalho;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

// Padrão Abstract Factory - produto concreto (família MySQL).
// O estado do livro vai como texto na coluna "estado" e volta com Livro.reconstituir().
public class RepositorioLivroJdbc implements RepositorioLivro {
    private final UnidadeDeTrabalhoJdbc unidade;

    RepositorioLivroJdbc(UnidadeDeTrabalhoJdbc unidade) {
        this.unidade = unidade;
    }

    private Connection c() {
        return unidade.conexao();
    }

    @Override
    public Optional<Livro> buscar(String idLivro) {
        return Sql.um(c(), "SELECT * FROM livro WHERE id = ?", RepositorioLivroJdbc::mapear, idLivro);
    }

    @Override
    public List<Livro> listar() {
        return Sql.listar(c(), "SELECT * FROM livro ORDER BY id", RepositorioLivroJdbc::mapear);
    }

    @Override
    public void salvar(Livro l) {
        Sql.salvar(c(),
                "UPDATE livro SET titulo = ?, autor = ?, estado = ? WHERE id = ?",
                new Object[]{l.getTitulo(), l.getAutor(), l.getEstado(), l.getId()},
                "INSERT INTO livro (id, titulo, autor, estado) VALUES (?, ?, ?, ?)",
                new Object[]{l.getId(), l.getTitulo(), l.getAutor(), l.getEstado()});
    }

    @Override
    public void remover(String idLivro) {
        Sql.executar(c(), "DELETE FROM livro WHERE id = ?", idLivro);
    }

    @Override
    public UnidadeDeTrabalho unidadeDeTrabalho() {
        return unidade;
    }

    private static Livro mapear(ResultSet rs) throws SQLException {
        return Livro.reconstituir(rs.getString("id"), rs.getString("titulo"), rs.getString("autor"),
                rs.getString("estado"));
    }
}
