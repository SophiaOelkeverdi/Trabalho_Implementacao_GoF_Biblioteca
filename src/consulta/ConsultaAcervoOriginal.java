package consulta;

import acesso.ExecutorTransacional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

// Padrão Decorator - papel ConcreteComponent: a consulta original, sem cache e sem medição
public class ConsultaAcervoOriginal implements ConsultaAcervo {
    private final ExecutorTransacional transacao;

    public ConsultaAcervoOriginal(ExecutorTransacional transacao) {
        this.transacao = transacao;
    }

    @Override
    public Optional<MetadadosLivro> consultarMetadados(String idLivro) {
        return transacao.executar(ctx -> ctx.acervo().consultarLivro(idLivro)
                .map(l -> new MetadadosLivro(l.getId(), l.getTitulo(), l.getAutor())));
    }

    @Override
    public List<MetadadosLivro> pesquisarPorTitulo(String trecho) {
        String t = trecho.toLowerCase(Locale.ROOT);
        return transacao.executar(ctx -> ctx.acervo().listar().stream()
                .filter(l -> l.getTitulo().toLowerCase(Locale.ROOT).contains(t))
                .map(l -> new MetadadosLivro(l.getId(), l.getTitulo(), l.getAutor()))
                .toList());
    }
}
