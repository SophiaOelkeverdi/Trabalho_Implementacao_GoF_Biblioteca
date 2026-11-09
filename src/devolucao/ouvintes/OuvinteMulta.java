package devolucao.ouvintes;

import devolucao.EventoDevolucao;
import devolucao.OuvinteDevolucao;
import financeiro.ServicoDeMultas;

// Padrão Observer - observador concreto: pede a multa pro Financeiro (só se atrasou)
public class OuvinteMulta implements OuvinteDevolucao {
    private final ServicoDeMultas servico;

    public OuvinteMulta(ServicoDeMultas servico) {
        this.servico = servico;
    }

    @Override
    public String nome() {
        return "multa";
    }

    @Override
    public void aoRegistrarDevolucao(EventoDevolucao e) {
        if (e.atrasada()) {
            servico.emitirMulta(e.idDevolucao(), e.idEstudante(), e.cpfEstudante(), e.nomeEstudante(),
                    e.diasAtraso());
        }
    }
}
