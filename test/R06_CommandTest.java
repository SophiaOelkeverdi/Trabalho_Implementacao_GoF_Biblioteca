import circulacao.Comprovantes.ComprovanteRenovacao;
import circulacao.comando.ExecutorDeSolicitacoes.Resultado;
import circulacao.comando.SolicitacaoRenovacao;
import dominio.livro.TransicaoRecusadaException;
import dominio.movimentacao.Emprestimo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("R06 - Solicitacoes de circulacao como objetos (Command)")
class R06_CommandTest {

    @Test
    void R06_mesmaSolicitacaoDeRenovacaoExecutadaDuasVezesAcrescentaApenas30Dias() {
        Cenario c = new Cenario();
        c.emprestar("L1", "E1");
        LocalDate vencimentoOriginal = c.emprestimoAtivo("L1").orElseThrow().getDataVencimento();
        SolicitacaoRenovacao s = new SolicitacaoRenovacao("REQ-1", "E1", Instant.now(), "L1", "E1");

        Resultado<ComprovanteRenovacao> primeira = c.app.tratadorDeEmprestimo().submeter(s);
        Resultado<ComprovanteRenovacao> segunda = c.app.tratadorDeEmprestimo().submeter(s);

        assertFalse(primeira.repetida());
        assertTrue(segunda.repetida());
        assertEquals(primeira.comprovante(), segunda.comprovante());
        Emprestimo e = c.emprestimoAtivo("L1").orElseThrow();
        assertEquals(vencimentoOriginal.plusDays(30), e.getDataVencimento());
        assertEquals(1, e.getNumRenovacoes());
    }

    @Test
    void R06_solicitacoesDiferentesContinuamSujeitasAoLimiteDeDuasRenovacoes() {
        Cenario c = new Cenario();
        c.emprestar("L1", "E1");
        LocalDate vencimentoOriginal = c.emprestimoAtivo("L1").orElseThrow().getDataVencimento();

        c.app.tratadorDeEmprestimo().submeter(new SolicitacaoRenovacao("REQ-1", "E1", Instant.now(), "L1", "E1"));
        c.app.tratadorDeEmprestimo().submeter(new SolicitacaoRenovacao("REQ-2", "E1", Instant.now(), "L1", "E1"));
        assertThrows(TransicaoRecusadaException.class, () -> c.app.tratadorDeEmprestimo()
                .submeter(new SolicitacaoRenovacao("REQ-3", "E1", Instant.now(), "L1", "E1")));

        assertEquals(vencimentoOriginal.plusDays(60), c.emprestimoAtivo("L1").orElseThrow().getDataVencimento());
    }

    @Test
    void R06_submissoesSimultaneasComOMesmoIdentificadorExecutamUmaUnicaVez() throws Exception {
        Cenario c = new Cenario();
        c.emprestar("L1", "E1");
        SolicitacaoRenovacao s = new SolicitacaoRenovacao("REQ-CONC", "E1", Instant.now(), "L1", "E1");
        int threads = 8;
        CountDownLatch largada = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        List<Future<Resultado<ComprovanteRenovacao>>> futuros = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            futuros.add(pool.submit(() -> {
                largada.await();
                return c.app.tratadorDeEmprestimo().submeter(s);
            }));
        }
        largada.countDown();
        int executadas = 0;
        for (Future<Resultado<ComprovanteRenovacao>> f : futuros) {
            if (!f.get().repetida()) {
                executadas++;
            }
        }
        pool.shutdown();

        assertEquals(1, executadas);
        assertEquals(1, c.emprestimoAtivo("L1").orElseThrow().getNumRenovacoes());
    }

    @Test
    void R06_solicitacaoGuardaIdentificadorAutorEInstante() {
        Instant agora = Instant.parse("2026-11-02T10:00:00Z");
        SolicitacaoRenovacao s = new SolicitacaoRenovacao("REQ-9", "AT1", agora, "L1", "E1");
        assertEquals("REQ-9", s.getId());
        assertEquals("AT1", s.getAutor());
        assertEquals(agora, s.getCriadaEm());
    }
}
