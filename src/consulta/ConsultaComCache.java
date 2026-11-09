package consulta;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

// Padrão Decorator - decorador concreto: cache de metadados por id do livro.
// - só guarda MetadadosLivro (sem estado), então não participa da decisão de reserva
//   (a fachada sempre lê o Livro atual pelo Acervo)
// - se o cache der erro, segue sem ele e consulta o componente de dentro
// - pesquisarPorTitulo não tem cache (usa o repasse do DecoradorConsulta)
public class ConsultaComCache extends DecoradorConsulta {
    private final Map<String, MetadadosLivro> armazenamento;
    private int acertos;
    private int falhasDeCache;

    public ConsultaComCache(ConsultaAcervo interna) {
        this(interna, new ConcurrentHashMap<>());
    }

    // dá pra passar outro mapa (no teste usamos um que dá erro)
    public ConsultaComCache(ConsultaAcervo interna, Map<String, MetadadosLivro> armazenamento) {
        super(interna);
        this.armazenamento = armazenamento;
    }

    @Override
    public Optional<MetadadosLivro> consultarMetadados(String idLivro) {
        try {
            MetadadosLivro emCache = armazenamento.get(idLivro);
            if (emCache != null) {
                acertos++;
                return Optional.of(emCache);
            }
        } catch (RuntimeException e) {
            falhasDeCache++;            // cache com problema: segue sem ele
        }
        Optional<MetadadosLivro> resultado = interna.consultarMetadados(idLivro);
        resultado.ifPresent(m -> {
            try {
                armazenamento.put(idLivro, m);
            } catch (RuntimeException e) {
                falhasDeCache++;
            }
        });
        return resultado;
    }

    public void invalidar(String idLivro) {
        try {
            armazenamento.remove(idLivro);
        } catch (RuntimeException e) {
            falhasDeCache++;
        }
    }

    public int acertos() {
        return acertos;
    }

    public int falhasDeCache() {
        return falhasDeCache;
    }
}
