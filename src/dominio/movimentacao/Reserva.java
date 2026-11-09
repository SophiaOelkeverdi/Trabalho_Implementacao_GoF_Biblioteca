package dominio.movimentacao;

import java.time.LocalDate;

public class Reserva extends Transacao {
    private final LocalDate validaAte;

    public Reserva(String id, LocalDate data, String idLivro, String idEstudante, LocalDate validaAte) {
        super(id, data, idLivro, idEstudante);
        this.validaAte = validaAte;
    }

    public LocalDate getValidaAte() { return validaAte; }

    public boolean pertenceA(String idEstudante) {
        return getIdEstudante().equals(idEstudante);
    }

    @Override
    public String toString() {
        return "Reserva " + getIdTransacao() + " livro=" + getIdLivro() + " estudante=" + getIdEstudante()
                + " valida ate " + validaAte;
    }
}
