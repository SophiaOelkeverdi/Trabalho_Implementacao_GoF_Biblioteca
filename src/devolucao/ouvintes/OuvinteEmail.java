package devolucao.ouvintes;

import devolucao.EventoDevolucao;
import devolucao.OuvinteDevolucao;
import devolucao.ServicoEmail;

// Padrão Observer - observador concreto: manda o comprovante de devolução por e-mail
public class OuvinteEmail implements OuvinteDevolucao {
    private final ServicoEmail email;

    public OuvinteEmail(ServicoEmail email) {
        this.email = email;
    }

    @Override
    public String nome() {
        return "email";
    }

    @Override
    public void aoRegistrarDevolucao(EventoDevolucao e) {
        String corpo = "Ola, " + e.nomeEstudante() + ". Recebemos o livro \"" + e.tituloLivro() + "\" em "
                + e.dataDevolucao() + " (vencimento " + e.vencimento() + ")."
                + (e.atrasada() ? " Atraso de " + e.diasAtraso() + " dia(s): o boleto sera enviado." : "");
        email.enviar(e.emailEstudante(), "Comprovante de devolucao", corpo);
    }
}
