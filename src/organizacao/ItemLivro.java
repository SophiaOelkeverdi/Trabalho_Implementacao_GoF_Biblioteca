package organizacao;

import java.util.List;

// Padrão Composite - papel Leaf.
// Guarda só o ID do livro, não o objeto Livro. Por isso mover o item na árvore
// não mexe no Livro (id, estado) nem na Movimentacao (empréstimos).
public class ItemLivro extends NoAcervo {
    private final String idLivro;

    public ItemLivro(String idLivro) {
        this.idLivro = idLivro;
    }

    public String getIdLivro() {
        return idLivro;
    }

    @Override
    public int contarLivros() {
        return 1;
    }

    @Override
    public List<String> listarLivros() {
        return List.of(idLivro);
    }

    @Override
    public String nome() {
        return "Livro " + idLivro;
    }

    @Override
    public String desenhar(String recuo) {
        return recuo + "- " + nome() + "\n";
    }
}
