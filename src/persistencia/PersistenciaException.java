package persistencia;

// Erro técnico de acesso a dados (ex: SQLException)
public class PersistenciaException extends RuntimeException {
    public PersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }

    public PersistenciaException(String mensagem) {
        super(mensagem);
    }
}
