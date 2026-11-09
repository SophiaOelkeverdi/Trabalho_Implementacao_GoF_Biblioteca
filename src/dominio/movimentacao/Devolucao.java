package dominio.movimentacao;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Devolucao extends Transacao {
    private final String idEmprestimo;
    private final LocalDate vencimento;

    public Devolucao(String id, LocalDate dataDevolucao, String idLivro, String idEstudante,
                     String idEmprestimo, LocalDate vencimento) {
        super(id, dataDevolucao, idLivro, idEstudante);
        this.idEmprestimo = idEmprestimo;
        this.vencimento = vencimento;
    }

    // dias corridos entre vencimento e devolução (nunca negativo)
    public long diasAtraso() {
        return Math.max(0, ChronoUnit.DAYS.between(vencimento, getData()));
    }

    public String getIdEmprestimo() { return idEmprestimo; }
    public LocalDate getVencimento() { return vencimento; }
    public LocalDate getDataDevolucao() { return getData(); }
}
