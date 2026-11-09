package circulacao.comando;

import circulacao.FachadaCirculacao;

import java.time.Instant;
import java.util.Objects;

// Padrão Command - papel Command.
// Uma solicitação de circulação vira um objeto com id, autor, instante de criação e executar().
//   - a tela (Client) cria a solicitação
//   - o ExecutorDeSolicitacoes (Invoker) decide se pode rodar
//   - a FachadaCirculacao / Livro (Receiver) aplicam as regras
// Não tem desfazer(): reserva e renovação são fatos de negócio. Pra "voltar atrás" existe outra
// operação do domínio (ex: cancelar reserva), com as regras dela.
public abstract class SolicitacaoCirculacao<R> {
    private final String id;
    private final String autor;
    private final Instant criadaEm;

    protected SolicitacaoCirculacao(String id, String autor, Instant criadaEm) {
        this.id = Objects.requireNonNull(id, "id");
        this.autor = Objects.requireNonNull(autor, "autor");
        this.criadaEm = Objects.requireNonNull(criadaEm, "criadaEm");
    }

    // só o ExecutorDeSolicitacoes chama (mesmo pacote)
    protected abstract R executar(FachadaCirculacao receptor);

    public abstract String descricao();

    public String getId() { return id; }
    public String getAutor() { return autor; }
    public Instant getCriadaEm() { return criadaEm; }

    @Override
    public String toString() {
        return descricao() + " [id=" + id + ", autor=" + autor + ", criada em " + criadaEm + "]";
    }
}
