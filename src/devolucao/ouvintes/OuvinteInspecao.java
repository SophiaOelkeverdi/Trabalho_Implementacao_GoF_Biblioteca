package devolucao.ouvintes;

import devolucao.EventoDevolucao;
import devolucao.FilaDeInspecao;
import devolucao.OuvinteDevolucao;

// Padrão Observer - observador concreto: coloca o livro devolvido na fila de inspeção
public class OuvinteInspecao implements OuvinteDevolucao {
    private final FilaDeInspecao fila;

    public OuvinteInspecao(FilaDeInspecao fila) {
        this.fila = fila;
    }

    @Override
    public String nome() {
        return "inspecao";
    }

    @Override
    public void aoRegistrarDevolucao(EventoDevolucao evento) {
        fila.adicionar(evento.idLivro());
    }
}
