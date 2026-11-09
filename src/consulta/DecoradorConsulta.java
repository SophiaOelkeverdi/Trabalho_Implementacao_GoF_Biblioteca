package consulta;

import java.util.List;
import java.util.Optional;

// Padrão Decorator - papel Decorator.
// Implementa o mesmo contrato e só repassa pro componente de dentro.
// As subclasses acrescentam o recurso (cache, medição).
public abstract class DecoradorConsulta implements ConsultaAcervo {
    protected final ConsultaAcervo interna;

    protected DecoradorConsulta(ConsultaAcervo interna) {
        this.interna = interna;
    }

    @Override
    public Optional<MetadadosLivro> consultarMetadados(String idLivro) {
        return interna.consultarMetadados(idLivro);
    }

    @Override
    public List<MetadadosLivro> pesquisarPorTitulo(String trecho) {
        return interna.pesquisarPorTitulo(trecho);
    }
}
