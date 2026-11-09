package circulacao.comando;

import circulacao.Comprovantes.ComprovanteRenovacao;
import circulacao.FachadaCirculacao;

import java.time.Instant;
import java.util.UUID;

// Padrão Command - comando concreto: renovação
public class SolicitacaoRenovacao extends SolicitacaoCirculacao<ComprovanteRenovacao> {
    private final String idLivro;
    private final String idEstudante;

    public SolicitacaoRenovacao(String id, String autor, Instant criadaEm, String idLivro, String idEstudante) {
        super(id, autor, criadaEm);
        this.idLivro = idLivro;
        this.idEstudante = idEstudante;
    }

    public static SolicitacaoRenovacao nova(String autor, String idLivro, String idEstudante) {
        return new SolicitacaoRenovacao(UUID.randomUUID().toString(), autor, Instant.now(), idLivro, idEstudante);
    }

    @Override
    protected ComprovanteRenovacao executar(FachadaCirculacao receptor) {
        return receptor.renovar(idLivro, idEstudante);
    }

    @Override
    public String descricao() {
        return "Renovacao do emprestimo do livro " + idLivro + " de " + idEstudante;
    }
}
