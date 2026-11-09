package financeiro.cobranca;

// Resposta da emissão do boleto, no formato da instituição
public record RespostaCobranca(String idCobranca, String linhaDigitavel, SituacaoCobranca situacao) {
}
