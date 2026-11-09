package consulta;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

// Padrão Decorator - decorador concreto: mede o tempo de cada chamada.
// Como fica POR FORA do cache, mede também as consultas que o cache respondeu.
public class ConsultaComMedicao extends DecoradorConsulta {
    private final RegistroDeMedicoes medicoes;

    public ConsultaComMedicao(ConsultaAcervo interna, RegistroDeMedicoes medicoes) {
        super(interna);
        this.medicoes = medicoes;
    }

    @Override
    public Optional<MetadadosLivro> consultarMetadados(String idLivro) {
        return medir("consultarMetadados", () -> interna.consultarMetadados(idLivro));
    }

    @Override
    public List<MetadadosLivro> pesquisarPorTitulo(String trecho) {
        return medir("pesquisarPorTitulo", () -> interna.pesquisarPorTitulo(trecho));
    }

    private <T> T medir(String operacao, Supplier<T> chamada) {
        long inicio = System.nanoTime();
        try {
            return chamada.get();
        } finally {
            medicoes.registrar(operacao, System.nanoTime() - inicio);
        }
    }
}
