package organizacao;

import java.util.List;

// Padrão Composite - papel Component.
// Grupos (unidade, seção, subseção) e itens (livro) respondem às mesmas operações.
public abstract class NoAcervo {
    private GrupoAcervo pai;

    public abstract int contarLivros();

    public abstract List<String> listarLivros();   // ids dos livros

    public abstract String nome();

    public abstract String desenhar(String recuo);  // desenha a árvore em texto

    public GrupoAcervo getPai() {
        return pai;
    }

    void definirPai(GrupoAcervo pai) {
        this.pai = pai;
    }

    // sobe até a raiz da árvore
    public NoAcervo raiz() {
        NoAcervo n = this;
        while (n.getPai() != null) {
            n = n.getPai();
        }
        return n;
    }
}
