package devolucao;

import java.util.ArrayList;
import java.util.List;

// Servidor de e-mail de mentira: só guarda as mensagens numa lista.
// Dá pra deixar "fora do ar" pra testar a falha de um ouvinte.
public class ServicoEmailSimulado implements ServicoEmail {

    public record Mensagem(String para, String assunto, String corpo) { }

    private final List<Mensagem> enviadas = new ArrayList<>();
    private volatile boolean foraDoAr;

    @Override
    public synchronized void enviar(String para, String assunto, String corpo) {
        if (foraDoAr) {
            throw new IllegalStateException("servidor de e-mail indisponivel");
        }
        enviadas.add(new Mensagem(para, assunto, corpo));
    }

    public void definirForaDoAr(boolean foraDoAr) {
        this.foraDoAr = foraDoAr;
    }

    public synchronized List<Mensagem> enviadas() {
        return List.copyOf(enviadas);
    }
}
