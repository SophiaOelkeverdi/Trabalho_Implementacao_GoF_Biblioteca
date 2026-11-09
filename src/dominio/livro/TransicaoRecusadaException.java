package dominio.livro;

import dominio.RegraDeNegocioException;

// Evento que o estado atual não aceita, ou guarda da transição falsa
public class TransicaoRecusadaException extends RegraDeNegocioException {
    private final String evento;
    private final String estado;

    public TransicaoRecusadaException(String evento, String estado) {
        this(evento, estado, "evento '" + evento + "' nao e aceito no estado " + estado);
    }

    public TransicaoRecusadaException(String evento, String estado, String motivo) {
        super("Operacao recusada: " + motivo);
        this.evento = evento;
        this.estado = estado;
    }

    public String getEvento() { return evento; }
    public String getEstado() { return estado; }
}
