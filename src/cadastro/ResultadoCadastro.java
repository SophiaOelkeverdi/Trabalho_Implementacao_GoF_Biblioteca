package cadastro;

// Resultado do incluir(): a entidade, se foi criada agora (ou já existia) e a mensagem
public record ResultadoCadastro<T>(T entidade, boolean novo, String mensagem) {
}
