package devolucao;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

// Padrão Observer - papel Subject (publicador).
// Cada ouvinte roda dentro do seu próprio try/catch: se um falhar (ex: e-mail fora do ar),
// os outros continuam e a devolução não é desfeita (ela já foi confirmada antes).
public class PublicadorDevolucao {
    private final List<OuvinteDevolucao> ouvintes = new CopyOnWriteArrayList<>();

    public void registrar(OuvinteDevolucao ouvinte) {
        ouvintes.add(ouvinte);
    }

    public void remover(OuvinteDevolucao ouvinte) {
        ouvintes.remove(ouvinte);
    }

    // devolve a lista de falhas (vazia se deu tudo certo)
    public List<String> publicar(EventoDevolucao evento) {
        List<String> falhas = new ArrayList<>();
        for (OuvinteDevolucao ouvinte : ouvintes) {
            try {
                ouvinte.aoRegistrarDevolucao(evento);
            } catch (RuntimeException e) {
                falhas.add(ouvinte.nome() + ": " + e.getMessage());
            }
        }
        return falhas;
    }

    public List<String> nomesDosOuvintes() {
        return ouvintes.stream().map(OuvinteDevolucao::nome).toList();
    }
}
