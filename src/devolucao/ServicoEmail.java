package devolucao;

// Envio de e-mail (infraestrutura externa)
public interface ServicoEmail {
    void enviar(String para, String assunto, String corpo);
}
