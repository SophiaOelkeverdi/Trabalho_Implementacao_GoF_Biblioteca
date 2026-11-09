package cadastro;

import dominio.RegraDeNegocioException;

// Dado inválido ou pré-requisito não atendido num cadastro
public class CadastroInvalidoException extends RegraDeNegocioException {
    public CadastroInvalidoException(String mensagem) {
        super(mensagem);
    }
}
