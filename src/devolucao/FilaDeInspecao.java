package devolucao;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

// Livros devolvidos esperando a inspeção (enquanto isso o livro fica DEVOLVIDO)
public class FilaDeInspecao {
    private final Set<String> pendentes = new LinkedHashSet<>();

    public synchronized void adicionar(String idLivro) {
        pendentes.add(idLivro);
    }

    public synchronized void remover(String idLivro) {
        pendentes.remove(idLivro);
    }

    public synchronized boolean contem(String idLivro) {
        return pendentes.contains(idLivro);
    }

    public synchronized List<String> pendentes() {
        return List.copyOf(pendentes);
    }
}
