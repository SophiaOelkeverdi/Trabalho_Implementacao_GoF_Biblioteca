package dominio;

// Lançada quando alguma regra do domínio é violada. A operação é recusada sem mudar nada.
public class RegraDeNegocioException extends RuntimeException {
    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
