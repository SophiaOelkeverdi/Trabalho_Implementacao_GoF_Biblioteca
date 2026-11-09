package organizacao;

import dominio.RegraDeNegocioException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Padrão Composite - papel Composite.
// Unidade, seção ou subseção (profundidade variável). Contar e listar percorrem a árvore
// recursivamente somando o resultado dos filhos.
//
// Regras do adicionar():
//  - ciclo: o filho não pode ser o próprio grupo nem um ancestral dele
//  - o filho não pode já ter outro pai
//  - nenhum livro do filho pode já estar em outro grupo da árvore
public class GrupoAcervo extends NoAcervo {

    public enum Tipo { UNIDADE, SECAO, SUBSECAO }

    private final String nome;
    private final Tipo tipo;
    private final List<NoAcervo> filhos = new ArrayList<>();

    public GrupoAcervo(String nome, Tipo tipo) {
        this.nome = nome;
        this.tipo = tipo;
    }

    public GrupoAcervo adicionar(NoAcervo filho) {
        if (filho == this || ehAncestral(filho)) {
            throw new RegraDeNegocioException("Ciclo recusado: " + filho.nome() + " ja contem " + nome);
        }
        if (filho.getPai() != null) {
            throw new RegraDeNegocioException(filho.nome() + " ja pertence ao grupo " + filho.getPai().nome());
        }
        List<String> livrosDaArvore = raiz().listarLivros();
        for (String idLivro : filho.listarLivros()) {
            if (livrosDaArvore.contains(idLivro)) {
                throw new RegraDeNegocioException("O livro " + idLivro + " ja pertence a outro grupo");
            }
        }
        filhos.add(filho);
        filho.definirPai(this);
        return this;
    }

    public GrupoAcervo adicionarLivro(String idLivro) {
        return adicionar(new ItemLivro(idLivro));
    }

    public void remover(NoAcervo filho) {
        if (filhos.remove(filho)) {
            filho.definirPai(null);
        }
    }

    // Move o livro pra outro grupo. Só troca o pai do ItemLivro na árvore,
    // não toca no Livro nem na Movimentacao.
    public void moverLivro(String idLivro, GrupoAcervo destino) {
        ItemLivro item = localizar(idLivro)
                .orElseThrow(() -> new RegraDeNegocioException("Livro " + idLivro + " nao esta em " + nome));
        GrupoAcervo origem = item.getPai();
        origem.remover(item);
        try {
            destino.adicionar(item);
        } catch (RuntimeException e) {
            origem.adicionar(item);   // deu errado: volta pro lugar
            throw e;
        }
    }

    public Optional<ItemLivro> localizar(String idLivro) {
        for (NoAcervo filho : filhos) {
            if (filho instanceof ItemLivro item && item.getIdLivro().equals(idLivro)) {
                return Optional.of(item);
            }
            if (filho instanceof GrupoAcervo g) {
                Optional<ItemLivro> achado = g.localizar(idLivro);
                if (achado.isPresent()) {
                    return achado;
                }
            }
        }
        return Optional.empty();
    }

    private boolean ehAncestral(NoAcervo candidato) {
        for (NoAcervo n = getPai(); n != null; n = n.getPai()) {
            if (n == candidato) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int contarLivros() {
        int total = 0;
        for (NoAcervo filho : filhos) {
            total += filho.contarLivros();   // recursão: grupo soma os filhos, item vale 1
        }
        return total;
    }

    @Override
    public List<String> listarLivros() {
        List<String> ids = new ArrayList<>();
        for (NoAcervo filho : filhos) {
            ids.addAll(filho.listarLivros());
        }
        return ids;
    }

    @Override
    public String nome() {
        return tipo + " " + nome;
    }

    @Override
    public String desenhar(String recuo) {
        StringBuilder sb = new StringBuilder(recuo + "+ " + nome() + " (" + contarLivros() + " livros)\n");
        for (NoAcervo filho : filhos) {
            sb.append(filho.desenhar(recuo + "   "));
        }
        return sb.toString();
    }

    public List<NoAcervo> filhos() {
        return List.copyOf(filhos);
    }

    public Tipo getTipo() {
        return tipo;
    }
}
