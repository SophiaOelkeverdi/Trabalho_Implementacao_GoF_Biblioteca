package circulacao.comando;

import circulacao.Comprovantes.ComprovanteReserva;
import circulacao.FachadaCirculacao;

import java.time.Instant;
import java.util.UUID;

// Padrão Command - comando concreto: reserva
public class SolicitacaoReserva extends SolicitacaoCirculacao<ComprovanteReserva> {
    private final String idLivro;
    private final String idEstudante;

    public SolicitacaoReserva(String id, String autor, Instant criadaEm, String idLivro, String idEstudante) {
        super(id, autor, criadaEm);
        this.idLivro = idLivro;
        this.idEstudante = idEstudante;
    }

    // cria uma solicitação nova com id aleatório
    public static SolicitacaoReserva nova(String autor, String idLivro, String idEstudante) {
        return new SolicitacaoReserva(UUID.randomUUID().toString(), autor, Instant.now(), idLivro, idEstudante);
    }

    @Override
    protected ComprovanteReserva executar(FachadaCirculacao receptor) {
        return receptor.reservar(idLivro, idEstudante);
    }

    @Override
    public String descricao() {
        return "Reserva do livro " + idLivro + " para " + idEstudante;
    }
}
