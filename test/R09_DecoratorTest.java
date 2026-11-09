import consulta.ConsultaAcervo;
import consulta.ConsultaAcervoOriginal;
import consulta.ConsultaComCache;
import consulta.ConsultaComMedicao;
import consulta.MetadadosLivro;
import consulta.RegistroDeMedicoes;
import dominio.livro.TransicaoRecusadaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("R09 - Servico de consulta com recursos opcionais (Decorator)")
class R09_DecoratorTest {

    /** Conta quantas vezes a consulta original foi realmente chamada. */
    static class Contadora implements ConsultaAcervo {
        final ConsultaAcervo alvo;
        final AtomicInteger chamadas = new AtomicInteger();

        Contadora(ConsultaAcervo alvo) { this.alvo = alvo; }

        @Override public Optional<MetadadosLivro> consultarMetadados(String id) {
            chamadas.incrementAndGet();
            return alvo.consultarMetadados(id);
        }

        @Override public List<MetadadosLivro> pesquisarPorTitulo(String t) { return alvo.pesquisarPorTitulo(t); }
    }

    @Test
    void R09_segundaConsultaDeMetadadosUsaCacheEAindaAssimGeraMedicao() {
        Cenario c = new Cenario();
        Contadora original = new Contadora(new ConsultaAcervoOriginal(c.app.transacao()));
        ConsultaComCache cache = new ConsultaComCache(original);
        RegistroDeMedicoes medicoes = new RegistroDeMedicoes();
        ConsultaAcervo cadeia = new ConsultaComMedicao(cache, medicoes);      // Medicao -> Cache -> Original

        MetadadosLivro primeira = cadeia.consultarMetadados("L1").orElseThrow();
        MetadadosLivro segunda = cadeia.consultarMetadados("L1").orElseThrow();

        assertEquals(primeira, segunda);
        assertEquals(1, original.chamadas.get(), "a segunda veio do cache");
        assertEquals(1, cache.acertos());
        assertEquals(2, medicoes.quantidade(), "as duas consultas foram medidas");
    }

    @Test
    void R09_resultadoAntigoDoCacheNuncaAutorizaReservaDeLivroQueDeixouDeEstarLivre() {
        Cenario c = new Cenario();
        // metadados de L1 entram no cache enquanto o livro esta LIVRE
        c.app.tratadorDeAcervo().consultarMetadados("L1");
        c.app.tratadorDeAcervo().consultarMetadados("L1");
        // L1 deixa de estar LIVRE
        c.app.fachada().reservar("L1", "E1");

        // o cache ainda responde os metadados...
        assertEquals("Livro 1", c.app.tratadorDeAcervo().consultarMetadados("L1").titulo());
        // ...mas a reserva consulta o estado atual do Livro e e recusada
        assertThrows(TransicaoRecusadaException.class, () -> c.app.tratadorDeEmprestimo().solicitarReserva("L1", "E2"));
        assertEquals("RESERVADO", c.estadoDoLivro("L1"));
        // e o cache nem tem como guardar o estado
        for (RecordComponent rc : MetadadosLivro.class.getRecordComponents()) {
            assertNotEquals("estado", rc.getName());
        }
    }

    @Test
    void R09_falhaDoCacheNaoImpedeAConsulta() {
        Cenario c = new Cenario();
        ConsultaComCache cacheQuebrado = new ConsultaComCache(new ConsultaAcervoOriginal(c.app.transacao()),
                new HashMap<>() {
                    @Override public MetadadosLivro get(Object k) { throw new IllegalStateException("cache fora"); }
                    @Override public MetadadosLivro put(String k, MetadadosLivro v) {
                        throw new IllegalStateException("cache fora");
                    }
                });

        Optional<MetadadosLivro> r = cacheQuebrado.consultarMetadados("L2");

        assertTrue(r.isPresent());
        assertEquals("Livro 2", r.get().titulo());
        assertTrue(cacheQuebrado.falhasDeCache() > 0);
    }
}
